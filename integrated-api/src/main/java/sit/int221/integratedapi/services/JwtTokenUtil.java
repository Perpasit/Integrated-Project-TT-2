package sit.int221.integratedapi.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import sit.int221.integratedapi.entities.User;

import java.io.Serializable;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.lang.System.currentTimeMillis;

@Component
public class JwtTokenUtil implements Serializable {

    @Autowired
    private UserService userService;

    private final Key SIGNING_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @Value("${jwt.token-validity-in-minutes}")
    private long jwtTokenValidityInMinutes;

    @Value("${jwt.issuer}")
    private String JWT_ISSUER;


    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.getUserByUsername(username);

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", JWT_ISSUER);
        claims.put("iat", currentTimeMillis());
        claims.put("exp", currentTimeMillis());
        claims.put("name", user.getName());
        claims.put("oid", user.getOid());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
        claims.put("sub", username);

        return doGenerateToken(claims, username);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.getUserByUsername(username);

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", JWT_ISSUER);
        claims.put("iat", currentTimeMillis());
        claims.put("exp", currentTimeMillis() + 24 * 60 * 60 * 1000);
        claims.put("oid", user.getOid());

        return doGenerateToken(claims, username);
    }


    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(currentTimeMillis()))
                .setExpiration(new Date(currentTimeMillis() + jwtTokenValidityInMinutes * 60 * 1000))
                .signWith(SIGNING_KEY)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }




}
