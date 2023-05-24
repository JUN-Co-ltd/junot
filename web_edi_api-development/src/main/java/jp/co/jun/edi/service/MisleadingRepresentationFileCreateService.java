package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TFileEntity;
import jp.co.jun.edi.entity.TMisleadingRepresentationFileEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.FileModel;
import jp.co.jun.edi.model.MisleadingRepresentationFileModel;
import jp.co.jun.edi.model.MisleadingRepresentationFilePostModel;
import jp.co.jun.edi.repository.TFileRepository;
import jp.co.jun.edi.repository.TMisleadingRepresentationFileRepository;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 優良誤認検査ファイル情報を作成するサービス.
 */
@Service
public class MisleadingRepresentationFileCreateService
        extends GenericCreateService<CreateServiceParameter<MisleadingRepresentationFilePostModel>, ListServiceResponse<MisleadingRepresentationFileModel>> {

    @Autowired
    private TMisleadingRepresentationFileRepository tMisRepFileRepository;

    @Autowired
    private TFileRepository tFileRepository;

    @Override
    protected ListServiceResponse<MisleadingRepresentationFileModel> execute(
            final CreateServiceParameter<MisleadingRepresentationFilePostModel> serviceParameter) {
        final MisleadingRepresentationFilePostModel misRepFilePostModel = serviceParameter.getItem();
        // 戻り値 MisleadingRepresentationFileModelのリスト
        final List<MisleadingRepresentationFileModel> misRepFileList = new ArrayList<MisleadingRepresentationFileModel>();

        List<FileModel> fileList = misRepFilePostModel.getFiles();
        List<BigInteger> itemList = misRepFilePostModel.getItems();

        if (Objects.isNull(itemList) || Objects.isNull(fileList)) {
            // 品番もファイルもない場合、入力不備エラーにする
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_M_001));
        }

        Set<BigInteger> set = new HashSet<>();
        boolean isDuplicate = itemList.stream()
                .filter(e -> !set.add(e))
                .findFirst()
                .isPresent();
        if (isDuplicate) {
            // 品番IDに重複があればエラー
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_M_002));
        }

        // ファイル情報登録
        for (final FileModel fileModel : fileList) {
            // ファイルIDに紐づくファイル情報を取得
            TFileEntity tFileEntity = tFileRepository.findByFileId(fileModel.getId()).orElseThrow(ResourceNotFoundException::new);
            // メモをセット
            tFileEntity.setMemo(fileModel.getMemo());
            // ファイル情報更新
            tFileRepository.save(tFileEntity);

         // 優良誤認検査ファイル情報登録
            // 品番
            for (final BigInteger partNoId : itemList) {
                final TMisleadingRepresentationFileEntity tMisRepFileEntity = new TMisleadingRepresentationFileEntity();
                // 品番IDをセット
                tMisRepFileEntity.setPartNoId(partNoId);
                // ファイルIDをセット
                tMisRepFileEntity.setFileNoId(fileModel.getId());
                tMisRepFileRepository.save(tMisRepFileEntity);

                final MisleadingRepresentationFileModel misRepFileModel = new MisleadingRepresentationFileModel();
                // 登録して採番されたIDを戻り値のMisleadingRepresentationFileModelのIDに設定する
                misRepFileModel.setId(tMisRepFileEntity.getId());
                // 登録したファイルをMisleadingRepresentationFileModelのファイル情報に設定する
                misRepFileModel.setFile(fileModel);
                // 登録したレコードをリストに追加
                misRepFileList.add(misRepFileModel);
            }
        }

        return ListServiceResponse.<MisleadingRepresentationFileModel>builder().items(misRepFileList).build();
    }
}
