package jp.co.jun.edi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TFileEntity;
import jp.co.jun.edi.model.FileModel;
import jp.co.jun.edi.repository.TFileRepository;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;

/**
 * ファイルテーブルにファイルを登録するService.
 */
@Service
public class FileCreateService extends GenericFileCreateService<CreateServiceParameter<FileModel>, CreateServiceResponse<FileModel>> {
    @Autowired
    private TFileRepository tFileRepository;

    @Override
    protected CreateServiceResponse<FileModel> execute(final CreateServiceParameter<FileModel> serviceParameter) {
        final FileModel file = serviceParameter.getItem();

        final TFileEntity tFile = new TFileEntity();

        tFile.setContentType(file.getContentType());
        tFile.setFileName(file.getFileName());
        tFile.setFileData(file.getFileData());
        tFile.setMemo(file.getMemo());

        tFileRepository.save(tFile);

        file.setId(tFile.getId());

        return CreateServiceResponse.<FileModel>builder().item(file).build();
    }
}
