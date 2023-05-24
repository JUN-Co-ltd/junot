package jp.co.jun.edi.service.maint.code;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.model.maint.code.MaintCodeListModel;
import jp.co.jun.edi.repository.MScreenStructureRepository;
import jp.co.jun.edi.service.GenericListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * メンテナンスコード情報のテーブルリストを取得する.
 */
@Service
public class MaintCodeListService extends GenericListService<ListServiceParameter<MaintCodeListModel>, ListServiceResponse<MaintCodeListModel>> {

    @Autowired
    private MScreenStructureRepository mScreenStructureRepository;

    @Override
    protected ListServiceResponse<MaintCodeListModel> execute(final ListServiceParameter<MaintCodeListModel> serviceParameter) {

        final List<MaintCodeListModel> list = mScreenStructureRepository
                .findByDeletedAtIsNull(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("tableId")))).stream().map(value -> {
                    final MaintCodeListModel model = new MaintCodeListModel();
                    BeanUtils.copyProperties(value, model);
                    return model;
                }).collect(Collectors.toList());

        return ListServiceResponse.<MaintCodeListModel>builder().items(list).build();
    }
}
