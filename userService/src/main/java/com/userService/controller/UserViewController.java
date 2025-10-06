package com.userService.controller;
import com.userService.dto.UserDto;
import com.userService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserViewController {
    private final UserService userService;

//    @GetMapping("/{userId}")
//    public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
//        // Здесь мы используем UserService, чтобы найти пользователя по ID
//        UserDto userDto = userService.findUserById(userId);
//        if (userDto != null) {
//            return new ResponseEntity<>(userDto, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
}