package lk.di47.ticket.feature.company.service;

import lk.di47.ticket.feature.company.dto.CompanyDetailRequest;
import lk.di47.ticket.feature.company.dto.CompanyResponse;
import lk.di47.ticket.feature.company.dto.CreateCompanyRequest;
import lk.di47.ticket.feature.company.dto.ListCompanyRequest;
import lk.di47.ticket.feature.company.dto.UpdateCompanyRequest;
import lk.di47.ticket.response.PageResponse;

public interface CompanyService {
    CompanyResponse create(CreateCompanyRequest request);

    PageResponse<CompanyResponse> list(ListCompanyRequest request);

    CompanyResponse detail(CompanyDetailRequest request);

    CompanyResponse update(UpdateCompanyRequest request);
}
