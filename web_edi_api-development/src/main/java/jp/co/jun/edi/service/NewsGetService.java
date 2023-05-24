package jp.co.jun.edi.service;

import java.math.BigInteger;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TNewsEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.NewsModel;
import jp.co.jun.edi.repository.TNewsRepository;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * お知らせ情報を取得するサービス.
 */
@Service
public class NewsGetService extends GenericGetService<GetServiceParameter<BigInteger>, GetServiceResponse<NewsModel>> {
    @Autowired
    private TNewsRepository tNewsRepository;

    @Override
    protected GetServiceResponse<NewsModel> execute(final GetServiceParameter<BigInteger> serviceParameter) {
        // お知らせ情報を取得し、データが存在しない場合は例外を投げる
        final TNewsEntity entity = tNewsRepository.findByIdAndDeletedAtIsNull(serviceParameter.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        final NewsModel newsModel = new NewsModel();

        BeanUtils.copyProperties(entity, newsModel);

        return GetServiceResponse.<NewsModel>builder().item(newsModel).build();
    }
}
