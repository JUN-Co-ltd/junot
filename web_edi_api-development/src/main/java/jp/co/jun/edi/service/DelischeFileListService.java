package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TDelischeFileInfoEntity;
import jp.co.jun.edi.model.DelischeFileInfoModel;
import jp.co.jun.edi.repository.TDelischeFileInfoRepository;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * デリスケファイル情報を取得するサービス.
 */
@Service
public class DelischeFileListService {

    @Autowired
    private TDelischeFileInfoRepository tDelischeFileInfoRepository;

    /**
     * ログインユーザーが作成したデリスケファイル情報リストを取得する.
     * @param userId ログインユーザーID
     * @return デリスケファイル情報リスト
     */
    public ListServiceResponse<DelischeFileInfoModel> execute(final BigInteger userId) {
        final List<DelischeFileInfoModel> list = new ArrayList<>();

        for (final TDelischeFileInfoEntity tDelischeFileInfoEntity : tDelischeFileInfoRepository.findByCreateUserId(userId,
                PageRequest.of(0, 1, Sort.by(Order.desc("createdAt"))))) {

            // 取得データのコピー
            final DelischeFileInfoModel delischeFileInfoModel = new DelischeFileInfoModel();
            BeanUtils.copyProperties(tDelischeFileInfoEntity, delischeFileInfoModel);

            // レスポンスに返却する
            list.add(delischeFileInfoModel);
        }

        return ListServiceResponse.<DelischeFileInfoModel>builder().items(list).build();
    }
}
