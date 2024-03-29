package com.myapp.security;

import com.myapp.repositories.UserRepo;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtTokenHelper jwtTokenHelper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //Get Token
        String requestToken= request.getHeader("Authorization");
        //Bearer
        System.out.println(requestToken);

        String userName= null;
        String token= null;

        if(request != null && requestToken.startsWith("Bearer")){
            token=requestToken.substring(7);
            try {
                userName=this.jwtTokenHelper.getUsernameFromToken(token);
            }catch (IllegalArgumentException e){
                System.out.println("Unable to get JWT token !");
            } catch (ExpiredJwtException e){
                System.out.println("JWT Token has expired"+e);
            } catch (MalformedJwtException e){
                System.out.println("Invalid Token"+e);
            }

        } else {
            System.out.println("JWT Bearer Token Doesn exist !");
        }
        //oncw we get the token, now validate
        if(userName !=null && SecurityContextHolder.getContext().getAuthentication()==null){
             UserDetails userDetails=this.userDetailsService.loadUserByUsername(userName);
            if(this.jwtTokenHelper.validateToken(token, userDetails)){
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken= new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

            }else {
                System.out.println("Invalid JWT token !");
            }

        }else {
            System.out.println("Username is null or context is not null !");
        }
        filterChain.doFilter(request, response);

    }
}
