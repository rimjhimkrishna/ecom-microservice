package com.example.userservice.exception; // Note: Wait! Let's make sure the package matches productservice!
// Ah! Let's double check package name: com.example.productservice.exception!
// Let me write com.example.productservice.exception.BusinessException.
package com.example.productservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
