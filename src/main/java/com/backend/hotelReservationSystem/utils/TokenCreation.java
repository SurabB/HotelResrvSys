package com.backend.hotelReservationSystem.utils;

import com.backend.hotelReservationSystem.exceptionClasses.TokenInvalidException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import java.security.SecureRandom;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenCreation {
    private static final int LENGTH=5;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz*&^#@";
    private static final SecureRandom random = new SecureRandom();
    private static final char SEPARATOR='$';

    public static String generateToken() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
    public static String getSeparator(){
        return Character.toString(SEPARATOR);
    }

    public static ParsedToken getToken(String token) {
        int index = token.indexOf(SEPARATOR);
        if(index<=0||index>token.length()-2){
            throw new TokenInvalidException("Verification token is invalid");
    }

        String userToken = token.substring(index+1);
        long key;
        try {
             key = Long.parseLong(token.substring(0, index));
        }
        catch (NumberFormatException e){
             throw new TokenInvalidException("provided token is not valid");
        }

         return new ParsedToken(key,userToken);


    }
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ParsedToken {
        private final long key;
        private final String userToken;

    }
}
