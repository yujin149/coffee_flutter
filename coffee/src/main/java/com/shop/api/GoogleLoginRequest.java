package com.shop.api;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {
    private String accessToken;
    private String idToken;
}
