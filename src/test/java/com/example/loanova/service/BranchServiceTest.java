package com.example.loanova.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.loanova.dto.request.BranchRequest;
import com.example.loanova.dto.response.BranchResponse;
import com.example.loanova.entity.Branch;
import com.example.loanova.exception.DuplicateResourceException;
import com.example.loanova.repository.BranchRepository;
import com.example.loanova.repository.LoanApplicationRepository;
import com.example.loanova.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

  @Mock private BranchRepository branchRepository;
  @Mock private UserRepository userRepository;
  @Mock private LoanApplicationRepository loanApplicationRepository;

  private BranchService branchService;

  @BeforeEach
  void setUp() {
    branchService = new BranchService(branchRepository, userRepository, loanApplicationRepository);
  }

  @Test
  void updateBranch_Success_WhenNoDuplicates() {
    // Arrange
    Long id = 1L;
    BranchRequest request = new BranchRequest();
    request.setBranchCode("B002");
    request.setBranchName("Branch 2");
    request.setAddress("Addr 2");

    Branch existingBranch = Branch.builder()
        .id(id)
        .branchCode("B001")
        .branchName("Branch 1")
        .address("Addr 1")
        .build();

    when(branchRepository.findById(id)).thenReturn(Optional.of(existingBranch));
    // Mock checks return false (no duplicates)
    when(branchRepository.existsByBranchCode("B002")).thenReturn(false);
    when(branchRepository.existsByBranchCodeAnyStatus("B002")).thenReturn(false);
    when(branchRepository.existsByBranchName("Branch 2")).thenReturn(false);
    when(branchRepository.existsByBranchNameAnyStatus("Branch 2")).thenReturn(false);
    
    when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    BranchResponse response = branchService.updateBranch(id, request);

    // Assert
    assertEquals("B002", response.getBranchCode());
    assertEquals("Branch 2", response.getBranchName());
    verify(branchRepository).save(any(Branch.class));
  }

  @Test
  void updateBranch_Fail_WhenDuplicateActiveCode() {
    Long id = 1L;
    BranchRequest request = new BranchRequest();
    request.setBranchCode("B002"); // Duplicate
    request.setBranchName("Branch 1");

    Branch existingBranch = Branch.builder().id(id).branchCode("B001").branchName("Branch 1").build();

    when(branchRepository.findById(id)).thenReturn(Optional.of(existingBranch));
    when(branchRepository.existsByBranchCode("B002")).thenReturn(true);

    assertThrows(DuplicateResourceException.class, () -> branchService.updateBranch(id, request));
  }

  @Test
  void updateBranch_Fail_WhenDuplicateSoftDeletedCode() {
    Long id = 1L;
    BranchRequest request = new BranchRequest();
    request.setBranchCode("B002"); // Duplicate deleted
    request.setBranchName("Branch 1");

    Branch existingBranch = Branch.builder().id(id).branchCode("B001").branchName("Branch 1").build();

    when(branchRepository.findById(id)).thenReturn(Optional.of(existingBranch));
    when(branchRepository.existsByBranchCode("B002")).thenReturn(false); // Not active
    when(branchRepository.existsByBranchCodeAnyStatus("B002")).thenReturn(true); // But exists in history

    DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, 
        () -> branchService.updateBranch(id, request));
    assertTrue(ex.getMessage().contains("sudah dihapus"));
  }

  @Test
  void updateBranch_Fail_WhenDuplicateActiveName() {
    Long id = 1L;
    BranchRequest request = new BranchRequest();
    request.setBranchCode("B001"); 
    request.setBranchName("Branch 2"); // Duplicate Name

    Branch existingBranch = Branch.builder().id(id).branchCode("B001").branchName("Branch 1").build();

    when(branchRepository.findById(id)).thenReturn(Optional.of(existingBranch));
    // Code didn't change, so no code check
    when(branchRepository.existsByBranchName("Branch 2")).thenReturn(true);

    assertThrows(DuplicateResourceException.class, () -> branchService.updateBranch(id, request));
  }

  @Test
  void updateBranch_Fail_WhenDuplicateSoftDeletedName() {
    Long id = 1L;
    BranchRequest request = new BranchRequest();
    request.setBranchCode("B001");
    request.setBranchName("Branch 2"); // Duplicate deleted Name

    Branch existingBranch = Branch.builder().id(id).branchCode("B001").branchName("Branch 1").build();

    when(branchRepository.findById(id)).thenReturn(Optional.of(existingBranch));
    when(branchRepository.existsByBranchName("Branch 2")).thenReturn(false);
    when(branchRepository.existsByBranchNameAnyStatus("Branch 2")).thenReturn(true);

    DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, 
        () -> branchService.updateBranch(id, request));
    assertTrue(ex.getMessage().contains("sudah dihapus"));
  }
}
