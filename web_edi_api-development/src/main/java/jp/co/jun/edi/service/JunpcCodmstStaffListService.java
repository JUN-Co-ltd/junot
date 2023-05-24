package jp.co.jun.edi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.repository.MCodmstStaffRepository;
import jp.co.jun.edi.repository.specification.JunpcCodmstStaffSpecification;
import jp.co.jun.edi.service.parameter.JunpcCodmstListServiceParameter;

/**
 * 発注生産システムのコードマスタから社員を検索するService.
 */
@Service
public class JunpcCodmstStaffListService extends GenericMCodmstListService {
    @Autowired
    private MCodmstStaffRepository mCodmstStaffRepository;

    @Autowired
    private JunpcCodmstStaffSpecification staffSpec;

    @Override
    protected Page<MCodmstEntity> find(final JunpcCodmstListServiceParameter serviceParameter) {

        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults(),
                Sort.by(Order.asc("code1")));

        return mCodmstStaffRepository.findAll(Specification
                .where(staffSpec.mntflgContains())
                .and(staffSpec.tblidContains(serviceParameter.getTblId().getValue()))
                .and(staffSpec.staffCodeOrStaffNameContains(
                        serviceParameter.getSearchCondition().getSearchType(),
                        serviceParameter.getSearchCondition().getSearchText()))
                .and(staffSpec.junotSearchTargetContains())
                .and(staffSpec.staffTypeInContains(
                        serviceParameter.getSearchCondition().getStaffType(),
                        serviceParameter.getSearchCondition().getSearchType()
                        ))
                .and(staffSpec.brandContains(
                        serviceParameter.getSearchCondition().getBrand(),
                        serviceParameter.getSearchCondition().getSearchType()
                        ))
                .and(staffSpec.notDeleteContains()),
                pageRequest);
    }
}
