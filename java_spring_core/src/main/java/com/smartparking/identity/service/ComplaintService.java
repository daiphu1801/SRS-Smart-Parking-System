package com.smartparking.identity.service;
import com.smartparking.identity.dto.request.ComplaintCreateRequest;
import com.smartparking.identity.dto.request.ComplaintFilterRequest;
import com.smartparking.identity.dto.response.ComplaintDetailResponse;
import com.smartparking.identity.entity.Complaint;
import com.smartparking.identity.repository.ComplaintRepository;
import com.smartparking.identity.repository.CustomerRepository;
import com.smartparking.identity.repository.EmployeeRepository;
import com.smartparking.identity.specification.ComplaintSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final CustomerRepository customerRepository; // Repo lấy thông tin khách
    private final EmployeeRepository employeeRepository; // Repo lấy thông tin admin/bảo vệ

    // ========================================================
    // LUỒNG DÀNH CHO KHÁCH HÀNG
    // ========================================================
    @Transactional
    public ComplaintDetailResponse createComplaint(Integer customerId, ComplaintCreateRequest request) {
        Complaint complaint = new Complaint();
        complaint.setCreatedBy(customerId);
        complaint.setContent(request.getContent());
        complaint.setImgUrl(request.getImgUrl()); // Đã sửa lỗi chính tả theo góp ý của tôi nhé
        complaint.setIsSolved(false); // Đã đổi thành Boolean

        Complaint savedComplaint = complaintRepository.save(complaint);
        log.info("Khách hàng ID {} vừa tạo khiếu nại ID {}", customerId, savedComplaint.getId());

        return mapToDetailResponse(savedComplaint);
    }

    // ========================================================
    // LUỒNG DÀNH CHO ADMIN / BẢO VỆ
    // ========================================================

    @Transactional(readOnly = true)
    public Page<ComplaintDetailResponse> getComplaintsWithFilter(ComplaintFilterRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Gọi Specification để tự động build câu query
        Page<Complaint> complaintPage = complaintRepository.findAll(
                ComplaintSpecification.buildFilterSpec(filter),
                pageable
        );

        // Map từng phần tử trong Page sang Response (Tự động join tên, sđt)
        return complaintPage.map(this::mapToDetailResponse);
    }

    @Transactional(readOnly = true)
    public ComplaintDetailResponse getComplaintById(Integer id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khiếu nại với ID: " + id));
        return mapToDetailResponse(complaint);
    }

    @Transactional
    public ComplaintDetailResponse solveComplaint(Integer id, Integer employeeId) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khiếu nại với ID: " + id));

        if (Boolean.TRUE.equals(complaint.getIsSolved())) {
            throw new RuntimeException("Khiếu nại này đã được xử lý trước đó!");
        }

        // Cập nhật trạng thái
        complaint.setIsSolved(true);
        complaint.setSolvedBy(employeeId);
        complaint.setSolvedAt(LocalDateTime.now());

        Complaint savedComplaint = complaintRepository.save(complaint);
        log.info("Nhân viên ID {} đã xử lý khiếu nại ID {}", employeeId, savedComplaint.getId());

        return mapToDetailResponse(savedComplaint);
    }

    // ========================================================
    // HÀM HELPER: LẮP GHÉP DỮ LIỆU (JOIN BẰNG CODE)
    // ========================================================
    private ComplaintDetailResponse mapToDetailResponse(Complaint complaint) {
        ComplaintDetailResponse response = new ComplaintDetailResponse();
        response.setId(complaint.getId());
        response.setContent(complaint.getContent());
        response.setImgUrl(complaint.getImgUrl());
        response.setCreatedAt(complaint.getCreatedAt());
        response.setSolvedAt(complaint.getSolvedAt());
        response.setIsSolved(complaint.getIsSolved());
        response.setCreatedBy(complaint.getCreatedBy());
        response.setSolvedBy(complaint.getSolvedBy());

        // Kéo thông tin Customer
        if (complaint.getCreatedBy() != null) {
            customerRepository.findById(complaint.getCreatedBy()).ifPresent(customer -> {
                response.setCustomerName(customer.getFullName()); // Tùy field của ông nhé
                response.setCustomerPhone(customer.getPhone());
            });
        }

        // Kéo thông tin Employee (Nếu đã được xử lý)
        if (complaint.getSolvedBy() != null) {
            employeeRepository.findById(complaint.getSolvedBy()).ifPresent(employee -> {
                response.setEmployeeName(employee.getFullName()); // Tùy field của ông nhé
                response.setEmployeePhone(employee.getPhone());
            });
        }

        return response;
    }
}