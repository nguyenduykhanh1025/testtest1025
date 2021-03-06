package vn.com.irtech.eport.framework.aspectj;

import java.lang.reflect.Method;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import vn.com.irtech.eport.common.annotation.Log;
import vn.com.irtech.eport.common.enums.BusinessStatus;
import vn.com.irtech.eport.common.json.JSON;
import vn.com.irtech.eport.common.utils.ServletUtils;
import vn.com.irtech.eport.common.utils.StringUtils;
import vn.com.irtech.eport.framework.domain.SysOperLog;
import vn.com.irtech.eport.framework.domain.SysUser;
import vn.com.irtech.eport.framework.manager.AsyncManager;
import vn.com.irtech.eport.framework.manager.factory.AsyncFactory;
import vn.com.irtech.eport.framework.util.ShiroUtils;

/**
 * Operation date record processing
 * 
 * @author admin
 */
@Aspect
@Component
public class LogAspect
{
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    // Placement point
    @Pointcut("@annotation(vn.com.irtech.eport.common.annotation.Log)")
    public void logPointCut()
    {
    }

    /**
     * After completing the processing request
     *
     * @param joinPoint 
     */
    @AfterReturning(pointcut = "logPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult)
    {
        handleLog(joinPoint, null, jsonResult);
    }

    /**
     * Normal operation
     * 
     * @param joinPoint 
     * @param e Exception
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e)
    {
        handleLog(joinPoint, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult)
    {
        try
        {
            // Annotation
            Log controllerLog = getAnnotationLog(joinPoint);
            if (controllerLog == null)
            {
                return;
            }

            SysUser currentUser = ShiroUtils.getSysUser();

            // *========A few days=========*//
            SysOperLog operLog = new SysOperLog();
            operLog.setStatus(BusinessStatus.SUCCESS.ordinal());

            String ip = ShiroUtils.getIp();
            operLog.setOperIp(ip);

            operLog.setJsonResult(JSON.marshal(jsonResult));

            operLog.setOperUrl(ServletUtils.getRequest().getRequestURI());
            if (currentUser != null)
            {
                operLog.setOperName(currentUser.getLoginName());
                if (StringUtils.isNotNull(currentUser.getDept())
                        && StringUtils.isNotEmpty(currentUser.getDept().getDeptName()))
                {
                    operLog.setDeptName(currentUser.getDept().getDeptName());
                }
            }

            if (e != null)
            {
                operLog.setStatus(BusinessStatus.FAIL.ordinal());
                operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }
            // Installation method name
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            // Installation request method
            operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            // The number of participants
            getControllerMethodDescription(controllerLog, operLog);
            // Number of preservation
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
        }
        catch (Exception exp)
        {
            // Recording book
            log.error("==Pre-notification exception==");
            log.error("Exception information: {}", exp.getMessage());
            exp.printStackTrace();
        }
    }

    /**
     * Get the description information of the method in the annotation Used for Controller layer annotation
     * 
     * @param log Log
     * @param operLog Operation log
     * @throws Exception
     */
    public void getControllerMethodDescription(Log log, SysOperLog operLog) throws Exception
    {
        // Set action
        operLog.setBusinessType(log.businessType().ordinal());
        // Set title
        operLog.setTitle(log.title());
        // Set operator category
        operLog.setOperatorType(log.operatorType().ordinal());
        // Do you need to save the request, parameters and values
        if (log.isSaveRequestData())
        {
            // Obtain the parameter information and pass it into the database.
            setRequestValue(operLog);
        }
    }

    /**
     * Get the parameters of the request and put it in the log
     * 
     * @param operLog Operation log
     * @throws Exception abnormal
     */
    private void setRequestValue(SysOperLog operLog) throws Exception
    {
        Map<String, String[]> map = ServletUtils.getRequest().getParameterMap();
        String params = JSON.marshal(map);
        operLog.setOperParam(StringUtils.substring(params, 0, 2000));
    }

    /**
     * Whether there is an annotation, if it exists, get it
     */
    private Log getAnnotationLog(JoinPoint joinPoint) throws Exception
    {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null)
        {
            return method.getAnnotation(Log.class);
        }
        return null;
    }
}
