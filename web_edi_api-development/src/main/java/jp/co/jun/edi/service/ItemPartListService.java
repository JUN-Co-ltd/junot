package jp.co.jun.edi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MItemPartsEntity;
import jp.co.jun.edi.model.ItemPartModel;
import jp.co.jun.edi.model.ItemPartSearchConditionModel;
import jp.co.jun.edi.repository.MItemPartsRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * パーツマスタからアイテム毎のパーツを検索するService.
 */
@Service
public class ItemPartListService extends GenericListService<ListServiceParameter<ItemPartSearchConditionModel>, ListServiceResponse<ItemPartModel>> {
    @Autowired
    private MItemPartsRepository mItemPartsRepository;

    @Override
    protected ListServiceResponse<ItemPartModel> execute(final ListServiceParameter<ItemPartSearchConditionModel> serviceParameter) {
        final Page<MItemPartsEntity> page = mItemPartsRepository.findByPartsNotIncludeSortOrderMinus(serviceParameter.getSearchCondition().getItemCode(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("sortOrder"))));

        final List<ItemPartModel> items = page.stream().map(
                entity -> {
                    final ItemPartModel item = new ItemPartModel();

                    // データをコピー
                    BeanUtils.copyProperties(entity, item);

                    return item;
                })
                .collect(Collectors.toList());

        return ListServiceResponse.<ItemPartModel>builder().items(items).build();
    }
}
