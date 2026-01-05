package com.example.loanova.controller;

import com.example.loanova.base.ApiResponse;
import com.example.loanova.dto.request.UserRequest;
import com.example.loanova.dto.request.UserUpdateRequest;
import com.example.loanova.dto.response.UserResponse;
import com.example.loanova.service.UserService;
import com.example.loanova.util.ResponseUtil;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  // GET ALL USERS
  @GetMapping
  public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
    List<UserResponse> users = userService.getAllUser();
    return ResponseUtil.ok(users, "Berhasil mengambil daftar pengguna");
  }

  // GET USER BY ID
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
    UserResponse user = userService.getUserById(id);
    return ResponseUtil.ok(user, "Berhasil mengambil data pengguna");
  }

  // CREATE USER
  @PostMapping
  public ResponseEntity<ApiResponse<UserResponse>> createUser(
      @Valid @RequestBody UserRequest request) {
    UserResponse user = userService.createUser(request);
    return ResponseUtil.created(user, "Berhasil membuat user");
  }

  // UPDATE USER
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> updateUser(
      @PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
    UserResponse user = userService.updateUser(id, request);
    return ResponseUtil.ok(user, "Berhasil memperbarui user");
  }

  // DELETE USER
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseUtil.ok(null, "Berhasil menghapus user");
  }
}
