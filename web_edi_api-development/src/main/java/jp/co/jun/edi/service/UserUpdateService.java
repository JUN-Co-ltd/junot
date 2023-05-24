package jp.co.jun.edi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.model.UserModel;
import jp.co.jun.edi.repository.MUserRepository;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;

/**
 * ユーザ情報を更新するService.
 * TODO 未使用
 */
@Service
public class UserUpdateService extends GenericUpdateService<UpdateServiceParameter<UserModel>, UpdateServiceResponse<UserModel>> {
    @Autowired
    private MUserRepository userRepository;

    @Override
    protected UpdateServiceResponse<UserModel> execute(final UpdateServiceParameter<UserModel> serviceParameter) {
        final MUserEntity user = userRepository.findByAccountNameAndCompany(
                serviceParameter.getItem().getAccountName(),
                serviceParameter.getItem().getCompany()).orElseThrow(ResourceNotFoundException::new);

        userRepository.save(user);

        return UpdateServiceResponse.<UserModel>builder().item(serviceParameter.getItem()).build();
    }
}
