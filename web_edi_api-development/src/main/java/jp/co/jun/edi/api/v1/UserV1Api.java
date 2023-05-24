package jp.co.jun.edi.api.v1;

import java.math.BigInteger;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.UserModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;

/**
 * TODO [未使用] ユーザAPI.
 */
public class UserV1Api {
    /**
     * ユーザを作成します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link UserModel} instance
     * @return {@link UserModel} instance
     */
    public UserModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(CreateValidationGroup.class) final UserModel item) {
        return item;
    }

    /**
     * ユーザを削除します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param userId ユーザID
     */
    public void delete(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("userId") final BigInteger userId) {
        return;
    }

    /**
     * ユーザを取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param userId ユーザID
     * @return {@link UserModel} instance
     */
    //@GetMapping("/{userId}")
    public UserModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("userId") final BigInteger userId) {
        return new UserModel();
    }

    /**
     * ユーザをリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @return {@link GenericListMobel} instance
     */
    public GenericListMobel<UserModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser) {
        return new GenericListMobel<UserModel>();
    }

    /**
     * ユーザを更新します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param userId ユーザID
     * @param item {@link UserModel} instance
     * @return {@link UserModel} instance
     */
    public UserModel update(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("userId") final BigInteger userId,
            @RequestBody @Validated(UpdateValidationGroup.class) final UserModel item) {
        return item;
    }
}
