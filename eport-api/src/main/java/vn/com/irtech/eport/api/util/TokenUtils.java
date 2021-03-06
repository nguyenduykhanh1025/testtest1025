package vn.com.irtech.eport.api.util;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import vn.com.irtech.eport.api.security.CustomUserDetails;
import vn.com.irtech.eport.common.utils.DateUtils;

@Service
public class TokenUtils {

	protected static final Logger logger = LoggerFactory.getLogger(TokenUtils.class);

	private static int EXPIRATION_TIME;

	private static String ISSUER;

	private static String SIGNING_KEY;

	private static Date EXPIRATION_DATE;

	@Value("${jwt.token-expiration-time:0}")
	public void setExpirationTime(int expirationTime) {
		EXPIRATION_TIME = expirationTime;
	}

	@Value("${jwt.token-issuer:@null}")
	public void setIssuer(String issuer) {
		ISSUER = issuer;
	}

	@Value("${jwt.token-signing-key:@null}")
	public void setSigningKey(String signingKey) {
		SIGNING_KEY = signingKey;
	}

	public static String generateToken(CustomUserDetails userDetails) {
		Date now = new Date();
		EXPIRATION_DATE = DateUtils.addSeconds(now, EXPIRATION_TIME);
		return Jwts.builder().setIssuer(ISSUER).setSubject(userDetails.getUser().getSubject())
				.setIssuedAt(now).setExpiration(EXPIRATION_DATE).signWith(SignatureAlgorithm.HS512, SIGNING_KEY).compact();
	}

	public static String getSubjectFromToken(String token) {
		try {
			Claims claims = Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(token).getBody();
			return claims.getSubject();
		} catch (Exception ex) {
			return null;
		}
	}

	public static boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(token);
			return true;
		} catch (MalformedJwtException ex) {
			logger.warn("Invalid JWT token");
		} catch (ExpiredJwtException ex) {
			logger.warn("Expired JWT token");
		} catch (UnsupportedJwtException ex) {
			logger.warn("Unsupported JWT token");
		} catch (IllegalArgumentException ex) {
			logger.warn("JWT claims string is empty");
		} catch (Exception ex) {
			logger.warn(ex.getMessage());
		}
		return false;
	}

	public static Date getExpirationDate() {
		return EXPIRATION_DATE;
	}
}
