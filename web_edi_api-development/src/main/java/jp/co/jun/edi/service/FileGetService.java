package jp.co.jun.edi.service;

import java.io.IOException;
import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.S3Component;
import jp.co.jun.edi.entity.TFileEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.model.FileModel;
import jp.co.jun.edi.repository.TFileRepository;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * ファイルテーブルからファイルを取得するService.
 */
@Service
@Slf4j
public class FileGetService extends GenericFileGetService<GetServiceParameter<BigInteger>, GetServiceResponse<FileModel>> {
    @Autowired
    private TFileRepository tFileRepository;

    @Autowired
    private S3Component s3Component;

    @Override
    protected GetServiceResponse<FileModel> execute(final GetServiceParameter<BigInteger> serviceParameter) {
        final FileModel fileModel = new FileModel();

        TFileEntity fileEntity = tFileRepository.findByFileId(serviceParameter.getId()).orElseThrow(ResourceNotFoundException::new);
        String s3Key = fileEntity.getS3Key();
        if (StringUtils.isNotEmpty(s3Key)) {
            // s3KeyがあればS3から取得
            String s3Prefix = fileEntity.getS3Prefix();
            byte[] fileData;
            try {
                fileData = s3Component.download(s3Prefix + "/" + s3Key);
            } catch (IOException e) {
                log.error("IOException occurred.", e);
                throw new ResourceNotFoundException(e);
            }
            // S3から取得したファイルデータ設定
            fileEntity.setFileData(fileData);
        }

        setEntityDataToModel(fileEntity, fileModel);

        return GetServiceResponse.<FileModel>builder().item(fileModel).build();
    }

    /**
     * ModelにEntityの値を設定する.
     * @param fileEntity {@link TFileEntity} instance
     * @param fileModel {@link FileModel} instance
     */
    private void setEntityDataToModel(final TFileEntity fileEntity, final FileModel fileModel) {
        fileModel.setContentType(fileEntity.getContentType());
        fileModel.setFileName(fileEntity.getFileName());
        fileModel.setFileData(fileEntity.getFileData());
    }
}
