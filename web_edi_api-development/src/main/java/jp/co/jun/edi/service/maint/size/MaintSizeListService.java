//PRD_0137 #10669 add start
package jp.co.jun.edi.service.maint.size;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.model.maint.MaintSizeListModel;
import jp.co.jun.edi.repository.MSizmstRepository;
import jp.co.jun.edi.service.GenericListService;
import jp.co.jun.edi.service.parameter.MaintSizeSearchServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 品種コードに紐づくサイズ情報を取得する.
 */
@Slf4j
@Service
public class MaintSizeListService extends GenericListService<MaintSizeSearchServiceParameter, ListServiceResponse<MaintSizeListModel>> {

    @Autowired
    private MSizmstRepository mSizmstRepository;

    @Override
    protected ListServiceResponse<MaintSizeListModel> execute(final MaintSizeSearchServiceParameter serviceParameter) {
        log.info(LogStringUtil.of("execute")
                .message("Start processing of SearchSizeMaster.")
                .build());
        final List<MaintSizeListModel> list = mSizmstRepository
                .findByHscdOrderByJunNoMntflg(serviceParameter.getHscd(), null).stream().map(value -> {
                    final MaintSizeListModel model = new MaintSizeListModel();
                    BeanUtils.copyProperties(value, model);
                    return model;
                }).collect(Collectors.toList());
        //99行になるまで空行追加
        if (list.size() != 0) {
            for (int RowCount = list.size(); RowCount < 99; RowCount++) {
        		final MaintSizeListModel resultRow = new MaintSizeListModel();
        		resultRow.setId("");
        		resultRow.setJun("");
        		resultRow.setSzkg("");
        		list.add(resultRow);
                }
        }
        log.info(LogStringUtil.of("execute")
                .message("End processing of SearchSizeMaster.")
                .build());

        return ListServiceResponse.<MaintSizeListModel>builder().items(list).build();
    }
}
//PRD_0137 #10669 add end