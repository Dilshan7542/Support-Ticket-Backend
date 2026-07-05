package lk.di47.ticket.feature.company.service;

import lk.di47.ticket.feature.company.dto.CompanyDetailRequest;
import lk.di47.ticket.feature.company.dto.CompanyResponse;
import lk.di47.ticket.feature.company.dto.CreateCompanyRequest;
import lk.di47.ticket.feature.company.dto.UpdateCompanyRequest;

import java.util.List;

public interface CompanyService {
    CompanyResponse create(CreateCompanyRequest request);

    List<CompanyResponse> list();

    CompanyResponse detail(CompanyDetailRequest request);

    CompanyResponse update(UpdateCompanyRequest request);
}
