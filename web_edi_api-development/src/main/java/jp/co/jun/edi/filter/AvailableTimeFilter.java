package jp.co.jun.edi.filter;

import java.io.IOException;
import java.time.ZonedDateTime;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import jp.co.jun.edi.component.AvailableTimeComponent;
import jp.co.jun.edi.model.AvailableTimeModel;
import jp.co.jun.edi.type.CustomHttpStatusType;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * JUNoT利用可能時間判定のフィルター.
 */
@Slf4j
public class AvailableTimeFilter extends OncePerRequestFilter {
    /**
     * 利用可能時間判定のコンポーネント.
     */
    private final AvailableTimeComponent availableTimeComponent;

    /**
     * コンストラクタ.
     *
     * @param availableTimeComponent {@link AvailableTimeComponent} instance
     */
    public AvailableTimeFilter(final AvailableTimeComponent availableTimeComponent) {
        this.availableTimeComponent = availableTimeComponent;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        final ZonedDateTime now = ZonedDateTime.now();

        if (availableTimeComponent.isUnavailableTime(now)) {
            final AvailableTimeModel model = new AvailableTimeModel();

            model.setAvailableTimes(availableTimeComponent.getAvailableTimes());
            model.setMessage(CustomHttpStatusType.UNAVAILABLE_TIME.getReasonPhrase());

            log.warn(LogStringUtil.of("doFilterInternal").value("response", model).build());

            response.setStatus(CustomHttpStatusType.UNAVAILABLE_TIME.value());
            response.getOutputStream().write(ObjectMapperUtil.writeValueAsBytes(model));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            return;
        }

        filterChain.doFilter(request, response);
    }
}
