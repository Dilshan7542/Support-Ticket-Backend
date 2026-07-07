package lk.di47.ticket.feature.company.service.impl;

import lk.di47.ticket.entity.Company;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.exception.NotFoundException;
import lk.di47.ticket.feature.company.dto.CompanyDetailRequest;
import lk.di47.ticket.feature.company.dto.CompanyResponse;
import lk.di47.ticket.feature.company.dto.CreateCompanyRequest;
import lk.di47.ticket.feature.company.dto.ListCompanyRequest;
import lk.di47.ticket.feature.company.dto.UpdateCompanyRequest;
import lk.di47.ticket.feature.company.service.CompanyService;
import lk.di47.ticket.repository.CompanyRepository;
import lk.di47.ticket.response.PageResponse;
import lk.di47.ticket.util.PaginationUtil;
import lk.di47.ticket.util.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public CompanyResponse create(CreateCompanyRequest request) {
        if (companyRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Company already exists");
        }
        if (companyRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Company code already exists");
        }

        Company company = new Company();
        company.setName(request.name());
        company.setCode(request.code());
        company.setDescription(request.description());
        company.setStatus(Status.ACTIVE);
        company.setCreatedAt(LocalDateTime.now());
        return toResponse(companyRepository.save(company));
    }

    @Override
    public PageResponse<CompanyResponse> list(ListCompanyRequest request) {
        return PageResponse.from(
                companyRepository.findAll(PaginationUtil.toPageable(request.page(), request.size())),
                this::toResponse
        );
    }

    @Override
    public CompanyResponse detail(CompanyDetailRequest request) {
        return toResponse(findCompany(request.companyId()));
    }

    @Override
    @Transactional
    public CompanyResponse update(UpdateCompanyRequest request) {
        Company company = findCompany(request.companyId());
        if (companyRepository.existsByNameAndIdNot(request.name(), company.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Company already exists");
        }
        if (companyRepository.existsByCodeAndIdNot(request.code(), company.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Company code already exists");
        }
        company.setName(request.name());
        company.setCode(request.code());
        company.setDescription(request.description());
        if (request.status() != null) {
            company.setStatus(request.status());
        }
        company.setUpdatedAt(LocalDateTime.now());
        return toResponse(companyRepository.save(company));
    }

    private Company findCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));
    }

    private CompanyResponse toResponse(Company company) {
        return new CompanyResponse(company.getId(), company.getName(), company.getCode(), company.getDescription(), company.getStatus());
    }
}
