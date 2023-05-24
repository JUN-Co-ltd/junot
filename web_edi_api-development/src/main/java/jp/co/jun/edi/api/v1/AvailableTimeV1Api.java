package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.AvailableTimeComponent;
import jp.co.jun.edi.model.AvailableTimeModel;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * JUNoT利用時間取得API.
 */
@RestController
@RequestMapping("/api/v1/availableTimes")
@Slf4j
public class AvailableTimeV1Api {
    @Autowired
    private AvailableTimeComponent availableTimeComponent;

    /**
     * JUNoT利用時間を取得します.
     *
     * @return {@link AvailableTimeModel} instance
     */
    @GetMapping()
    public AvailableTimeModel get() {
        final AvailableTimeModel model = new AvailableTimeModel();

        model.setAvailableTimes(availableTimeComponent.getAvailableTimes());

        log.info(LogStringUtil.of("get").value("response", model).build());

        return model;
    }
}
