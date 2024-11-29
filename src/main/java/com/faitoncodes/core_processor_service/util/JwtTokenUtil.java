package com.faitoncodes.core_processor_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;


@Component
public class JwtTokenUtil {

    @Autowired
    HttpServletRequest request;

    public Long getIdFromToken() {
        Claims claims = (Claims) request.getAttribute("claims");

        if (claims == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token não encontrado.");
        }

        Long userId = claims.get("userId", Long.class);
        if(userId != null){
            return userId;
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id de usuario não encontrado no token.");
        }
    }
}