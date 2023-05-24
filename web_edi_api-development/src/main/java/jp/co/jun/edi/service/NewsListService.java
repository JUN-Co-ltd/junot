package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TNewsEntity;
import jp.co.jun.edi.model.NewsModel;
import jp.co.jun.edi.model.NewsSearchConditionModel;
import jp.co.jun.edi.repository.TNewsRepository;
import jp.co.jun.edi.repository.specification.TNewsSpecification;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 条件を基にお知らせ情報を取得するサービス.
 */
@Service
public class NewsListService
        extends GenericListService<ListServiceParameter<NewsSearchConditionModel>, ListServiceResponse<NewsModel>> {
    @Autowired
    private TNewsSpecification newsSpec;

    @Autowired
    private TNewsRepository tNewsRepository;

    @Override
    protected ListServiceResponse<NewsModel> execute(final ListServiceParameter<NewsSearchConditionModel> serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults(),
                Sort.by(toSortOrder()));

        // 基準日を取得
        final Date referenceDate = new Date();

        final Page<TNewsEntity> page = tNewsRepository.findAll(Specification
                .where(newsSpec.notDeleteContains())
                .and(newsSpec.periodGreaterThanOpenStartAt(referenceDate))
                .and(newsSpec.openEndAtContains(referenceDate)),
                pageRequest);

        return ListServiceResponse.<NewsModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(entity -> toModel(entity)).collect(Collectors.toList()))
                .build();
    }

    /**
     * Model に変換する.
     *
     * @param entity {@link TNewsEntity} instance
     * @return {@link NewsModel} instance
     */
    protected NewsModel toModel(final TNewsEntity entity) {
        final NewsModel model = new NewsModel();

        model.setId(entity.getId());
        model.setTitle(entity.getTitle());
        model.setOpenStartAt(entity.getOpenStartAt());
        model.setOpenEndAt(entity.getOpenEndAt());
        model.setNewDisplayEndAt(entity.getNewDisplayEndAt());

        return model;
    }

    /**
     * ソート条件のリストを取得する.
     *
     * @return ソート済のList<Order>
     */
    private List<Order> toSortOrder() {
        final List<Order> orderList = new ArrayList<>();

        orderList.add(Order.desc("OpenStartAt"));
        orderList.add(Order.desc("id"));

        return orderList;
    }
}
