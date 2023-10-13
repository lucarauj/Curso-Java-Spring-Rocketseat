package br.com.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.todolist.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        //get user
        var authorization = request.getHeader("Authorization");
        var authEncoded = authorization.substring("Basic".length()).trim();
        byte[] authDecode = Base64.getDecoder().decode(authEncoded);
        var authString = new String(authDecode);
        String[] credentials = authString.split(":");
        String username = credentials[0];
        String password = credentials[1];

        //validate user
        var user = userRepository.findByUsername(username);

        if(user == null) {
            response.sendError(401, "Usuário não encontrado!");
        } else {
            //password validation
            var verification = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

            System.out.println(verification);
            System.out.println(verification.verified);

            if (verification.verified) {
                filterChain.doFilter(request, response);
            } else {
                response.sendError(401, "Senha incorreta para o usuário informado!");
            }
        }
    }
}