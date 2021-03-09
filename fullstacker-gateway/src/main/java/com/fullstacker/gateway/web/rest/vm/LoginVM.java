package me.fullstacker.gateway.web.rest.vm;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * View Model object for storing a user's credentials.
 */
@Data
public class LoginVM {

    @NotNull
    @Size(min = 1, max = 50)
    private String username;

   
    private String password;

    private Boolean rememberMe;
    
    private Boolean safeKickOut;
    
    private String moduleId;
    
}
