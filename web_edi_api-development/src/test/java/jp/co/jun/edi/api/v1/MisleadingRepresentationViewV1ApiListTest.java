package jp.co.jun.edi.api.v1;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.model.ItemMisleadingRepresentationSearchConditionModel;
import jp.co.jun.edi.type.EntireQualityApprovalType;
import test.utils.LoginUserUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class MisleadingRepresentationViewV1ApiListTest {
    // モックサーバー
    @Autowired
    private MockMvc mockServer;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    //@WithMockUser(username = "581209\n000001", password = "H3RYB9F8", roles = { "USER", "JUN" })
    public void test_list_0001() throws Exception {
        final MUserEntity user = createUser();
        final ItemMisleadingRepresentationSearchConditionModel test = createRequestModel();

        final MvcResult actual = mockServer
                .perform(MockMvcRequestBuilders.post("/api/v1/misleadingRepresentationsView")
                        .with(user(LoginUserUtils.generateLoginUser(user)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON) // ContentType設定
                        .content(objectMapper.writeValueAsString(test)) //JSON設定
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()) //HTTPレスポンスコード：200
                .andReturn();
        System.out.println(actual.getResponse().getContentAsString());
    }

    private ItemMisleadingRepresentationSearchConditionModel createRequestModel() {
        final ItemMisleadingRepresentationSearchConditionModel model = new ItemMisleadingRepresentationSearchConditionModel();
        String brandCode = null;
        String itemCode = null;
        String partNoKind = null;
        Integer year = null;
        List<EntireQualityApprovalType> qualityStatusList = null;
        List<String> subSeasonCodeList = null;
        Date approvalAtFrom = null;
        Date approvalAtTo = null;
        Date productCorrectionDeliveryAtFrom = null;
        Date productCorrectionDeliveryAtTo = null;



        model.setBrandCode(brandCode);
        model.setApprovalAtFrom(approvalAtFrom);
        model.setApprovalAtTo(approvalAtTo);
        model.setItemCode(itemCode);
        model.setPartNoKind(partNoKind);
        model.setProductCorrectionDeliveryAtFrom(productCorrectionDeliveryAtFrom);
        model.setProductCorrectionDeliveryAtTo(productCorrectionDeliveryAtTo);
        model.setQualityStatusList(qualityStatusList);
        model.setSubSeasonCodeList(subSeasonCodeList);
        model.setYear(year);

        model.setMaxResults(100);
        model.setPage(1);
        return model;
    }

    private MUserEntity createUser() {
        MUserEntity user = new MUserEntity();
        user.setId(BigInteger.valueOf(1));
        user.setAccountName("000001");
        user.setCompany("581209");
        user.setPassword("H3RYB9F8");
        user.setAuthority("ROLE_JUN,ROLE_USER");
        return user;
    }
}
