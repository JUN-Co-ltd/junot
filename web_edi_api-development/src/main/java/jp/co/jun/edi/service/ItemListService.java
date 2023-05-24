package jp.co.jun.edi.service;

import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.extended.ExtendedTItemListEntity;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.ItemSearchConditionModel;
import jp.co.jun.edi.repository.ItemCompositeRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.OrderByType;

/**
 * 画面で指定された検索条件を基に品番情報を取得するサービス.
 */
@Service
public class ItemListService extends GenericListService<ListServiceParameter<ItemSearchConditionModel>, ListServiceResponse<ItemModel>> {

    @Autowired
    private ItemCompositeRepository itemCompositeRepository;

    @Override
    protected ListServiceResponse<ItemModel> execute(final ListServiceParameter<ItemSearchConditionModel> serviceParameter) {

        final PageRequest itemPageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults(),
                Sort.by(sortByPriority(serviceParameter.getSearchCondition())));

        final Page<ExtendedTItemListEntity> page =
                itemCompositeRepository.findBySpec(serviceParameter.getSearchCondition(), serviceParameter.getLoginUser(), itemPageRequest);

        return ListServiceResponse.<ItemModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(entity -> {
                    final ItemModel model = new ItemModel();

                    // エンティティからモデルへコピー
                    BeanUtils.copyProperties(entity, model);

                    return model;
                }).collect(Collectors.toList()))
                .build();

    }

    /**
     * ソート設定値に応じたソートを返却.
     * @param itemSearchConditionModel ItemSearchConditionModel
     * @return 設定値に応じたソート
     */
    private Order sortByPriority(final ItemSearchConditionModel itemSearchConditionModel) {

        if (OrderByType.DESC == OrderByType.convertToType(itemSearchConditionModel.getIdOrderBy())) {
            return Order.desc("id");
        } else {
            return Order.asc("id");
        }
    }
}
