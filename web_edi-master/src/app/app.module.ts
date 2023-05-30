import { NgModule } from '@angular/core';
import { CommonModule, registerLocaleData } from '@angular/common';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import localeja from '@angular/common/locales/ja';

import { CookieService } from 'ngx-cookie-service';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { ScrollEventModule } from 'ngx-scroll-event';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { library } from '@fortawesome/fontawesome-svg-core';
import { fas } from '@fortawesome/free-solid-svg-icons';
import { far } from '@fortawesome/free-regular-svg-icons';

import { StorageServiceModule } from 'angular-webstorage-service';

import { AppRoutingModule } from './app-routing.module';
import { HttpApiInterceptor } from './interceptor/http-api-interceptor';

import { HeaderService } from './service/header.service';
import { LoadingService } from './service/loading.service';
import { SwitchTabService } from './service/switch-tab.service';

import { AppComponent } from './app.component';
import { ItemComponent } from './component/item/item.component';
import { LoginComponent } from './component/login/login.component';
import { MaintNewsListComponent } from 'src/app/component/maint/maint-news-list/maint-news-list.component';
import { MaintNewsComponent } from 'src/app/component/maint/maint-news/maint-news.component';
import { MaintTopComponent } from 'src/app/component/maint/maint-top/maint-top.component';
import { HeaderComponent } from './component/header/header.component';
import { SkuSelectComponent } from './component/item/sku-select/sku-select.component';
import { ItemListComponent } from './component/item-list/item-list.component';
import { SearchStaffModalComponent } from './component/search-staff-modal/search-staff-modal.component';
import { SearchColorModalComponent } from './component/search-color-modal/search-color-modal.component';
import { OrderComponent } from './component/order/order.component';
import { SearchKojmstModalComponent } from './component/search-kojmst-modal/search-kojmst-modal.component';
import { SkuInputComponent } from './component/sku-input/sku-input.component';
import { BreadcrumbComponent } from './component/breadcrumb/breadcrumb.component';
import { DeliveryRequestComponent } from './component/delivery-request/delivery-request.component';
import { OrderStatusListComponent } from './component/order-status-list/order-status-list.component';
import { ListComponent } from './component/list/list.component';
import { OrderListComponent } from './component/order-list/order-list.component';
import { OffTimesComponent } from './component/off-times/off-times.component';
import { ContactInformationComponent } from './component/contact-information/contact-information.component';
import { DeliveryListViewModalComponent } from './component/delivery-list-view-modal/delivery-list-view-modal.component';
import { ProductionStatusModalComponent } from './component/production-status-modal/production-status-modal.component';
import { MessageConfirmModalComponent } from './component/message-confirm-modal/message-confirm-modal.component';
import { SearchSupplierModalComponent } from './component/search-supplier-modal/search-supplier-modal.component';
import { DeliverySubmitConfirmModalComponent } from './component/delivery-submit-confirm-modal/delivery-submit-confirm-modal.component';
import { FabricInspectionResultComponent } from './component/fabric-inspection-result/fabric-inspection-result.component';
import { TopComponent } from './component/top/top.component';
import { MenuComponent } from './component/menu/menu.component';
import { NewsComponent } from './component/news/news.component';
import { NewsDetailComponent } from './component/news-detail/news-detail.component';
import { DeliveryPlanComponent } from './component/delivery-plan/delivery-plan.component';
import { DeliveryPlanDetailComponent } from './component/delivery-plan-detail/delivery-plan-detail.component';
import { DeliveryRequestHistoryComponent } from './component/delivery-request/delivery-request-history/delivery-request-history.component';
import { DelischeComponent } from './component/delische/delische.component';
import { SortIconComponent } from './component/delische/sort-icon/sort-icon.component';
import { FukukitaruMasterTextInputComponent } from './component/fukukitaru-master-text-input/fukukitaru-master-text-input.component';
import { DelischeRecordComponent } from './component/delische/delische-record/delische-record.component';
import { FukukitaruOrder01WashComponent } from './component/fukukitaru-order01-wash/fukukitaru-order01-wash.component';
import { FukukitaruOrder01HangTagComponent } from './component/fukukitaru-order01-hang-tag/fukukitaru-order01-hang-tag.component';
import { FukukitaruSkuInputComponent } from './component/fukukitaru-sku-input/fukukitaru-sku-input.component';
import { NumberInputPipe } from './pipe/number-input.pipe';
import { PartNoInputPipe } from './pipe/part-no-input.pipe';
import { MonthlyPipe } from './pipe/monthly.pipe';
import { SafeHtmlPipe } from './pipe/safe-html.pipe';
import { IsWithinOpenPeriodPipe } from './pipe/is-within-open-period.pipe';
import { IsNewNewsPipe } from './pipe/is-new-news.pipe';
import { DateInputPipe } from './pipe/date-input.pipe';
import { MdWeekPipe } from './pipe/md-week.pipe';
import { RatePipe } from './pipe/rate.pipe';
import { DeliveryCountPipe } from './pipe/delivery-count.pipe';
import { PurchaseStatusPipe } from './pipe/purchase-status.pipe';
//PRD_0133 #10181 add JFE start
import { PurchaseRecordStatusPipe } from './pipe/purchase-record-status.pipe';
//PRD_0133 #10181 add JFE end
import { ShippingInstructionsStatusPipe } from './pipe/shipping-instructions-status.pipe';

import { PartNoInputDirective } from './directive/part-no-input.directive';
import { NumberInputDirective } from './directive/number-input.directive';
import { FileDropDirective } from './directive/file-drop.directive';
import { DateInputDirective } from './directive/date-input.directive';
import { DeliveryHitoryDateDirective } from './directive/delivery-hitory-date.directive';
import { ConstTextAtSpecificValueDirective } from './directive/const-text-at-specific-value.directive';
import { CommonValidatorDirective } from './validator/material-order/common-validator.directive';
import { Brand01ValidatorDirective } from './validator/material-order/brand01-validator.directive';
import { Brand02ValidatorDirective } from './validator/material-order/brand02-validator.directive';
import { Brand03ValidatorDirective } from './validator/material-order/brand03-validator.directive';
import {
  DeliveryDistributionModalValidatorDirective
} from './component/delivery-distribution-modal/validator/delivery-distribution-modal-validator.directive';
import {
  MisleadingRepresentationValidatorDirective
} from './component/misleading-representation-list/validator/misleading-representation-validator.directive';
import { ProductionStatusValidatorDirective } from './component/production-status-modal/validator/production-status-validator.directive';
import { ItemValidatorDirective } from './component/item/validator/item-validator.directive';
import { FabricInspectionValidatorDirective } from './component/fabric-inspection-result/validator/fabric-inspection-validator.directive';
import { DeliveryPlanValidatorDirective } from './component/delivery-plan/validator/delivery-plan-validator.directive';
import { DeliveryRequestValidatorDirective } from './component/delivery-request/validator/delivery-request-validator.directive';
import { OrderValidatorDirective } from './component/order/validator/order-validator.directive';
import {
  FukukitaruOrder01HangTagValidatorDirective
} from './component/fukukitaru-order01-hang-tag/validator/fukukitaru-order01-hang-tag-validator.directive';
import {
  FukukitaruOrder01WashValidatorDirective
} from './component/fukukitaru-order01-wash/validator/fukukitaru-order01-wash-validator.directive';
import { FromToCheckDirective } from './directive/from-to-check.directive';
import { FromToCheck2Directive } from './directive/from-to-check2.directive';
import { FromToCheck3Directive } from './directive/from-to-check3.directive';
import { RequiredRelationDirective } from './directive/required-relation.directive';
import { RequiredRelation2Directive } from './directive/required-relation2.directive';
import { ArrayRequiredDirective } from './directive/array-required.directive';
import { OverBaseNumberDirective } from './directive/over-base-number.directive';

import { FukukitaruOrderModalComponent } from './component//fukukitaru-order-modal/fukukitaru-order-modal.component';
import { SearchCompanyModalComponent } from './component/search-company-modal/search-company-modal.component';
import {
  DeliveryPlanSubmitConfirmModalComponent
} from './component/delivery-plan/modal/delivery-plan-submit-confirm-modal/delivery-plan-submit-confirm-modal.component';
import { AppendicesTermModalComponent } from './component/appendices-term-modal/appendices-term-modal.component';
import { AttentionModalComponent } from './component/attention-modal/attention-modal.component';
import { FukukitaruInputAssistModalComponent } from './component/fukukitaru-input-assist-modal/fukukitaru-input-assist-modal.component';
import { OrderApprovalComponent } from './component/order-approval/order-approval.component';
import { MaintNewsStatusPipe } from './pipe/maint-news-status.pipe';
import { MaintNewsValidatorDirective } from 'src/app/component/maint/maint-news/validator/maint-news-validator.directive';
import { MaintUserListComponent } from 'src/app/component/maint/maint-user-list/maint-user-list.component';
import { MaintUserComponent } from 'src/app/component/maint/maint-user/maint-user.component';
// PRD_0141 #10656 add JFE start
import { MaintSireListComponent } from 'src/app/component/maint/maint-sire-list/maint-sire-list.component';
import { MaintSireListResultComponent } from 'src/app/component/maint/maint-sire-list/maint-sire-list-result/maint-sire-list-result.component';
import { MaintSireListFormComponent } from 'src/app/component/maint/maint-sire-list/maint-sire-list-form/maint-sire-list-form.component';
import { MaintSireComponent } from 'src/app/component/maint/maint-sire/maint-sire.component';
// PRD_0141 #10656 add JFE end
import { BulkRegistItemComponent } from './component/bulk-regist-item/bulk-regist-item.component';
import { ArticleNumberComponent } from './component/item/article-number/article-number.component';
import { LoadingComponent } from './component/loading/loading.component';
import { DeliveryDistributionModalComponent } from './component/delivery-distribution-modal/delivery-distribution-modal.component';
import {
  MisleadingRepresentationListComponent
} from 'src/app/component/misleading-representation-list/misleading-representation-list.component';
import { MisleadingApproveComponent } from './component/misleading-approve/misleading-approve.component';
import { MisleadingApproveFormComponent } from './component/misleading-approve/misleading-approve-form/misleading-approve-form.component';
import { DeliveryListComponent } from './component/delivery-list/delivery-list.component';
import { DeriveryStoreComponent } from './component/derivery-store/derivery-store.component';
import { DeliveryHeaderComponent } from './component/delivery-header/delivery-header.component';
import { DeliveryHeaderErrorMessageComponent } from './component/delivery-header-error-message/delivery-header-error-message.component';
import { DeriveryStoreValidatorDirective } from './component/derivery-store/validator/derivery-store-validator.directive';
import { ErrorModalComponent } from './component/error-modal/error-modal.component';
import { InventoryShipmentListComponent } from './component/inventory-shipment-list/inventory-shipment-list.component';
import { MaintCodeComponent } from 'src/app/component/maint/maint-code/maint-code.component';
import { DistributionShipmentListComponent } from './component/distribution-shipment-list/distribution-shipment-list.component';
import { MakerReturnComponent } from './component/maker-return/maker-return.component';
import { PurchaseComponent } from './component/purchase/purchase.component';
import { PurchaseListComponent } from './component/purchase-list/purchase-list.component';
import { PurchaseListFormComponent } from './component/purchase-list/purchase-list-form/purchase-list-form.component';
import { PurchaseListResultComponent } from './component/purchase-list/purchase-list-result/purchase-list-result.component';
//PRD_0133 #10181 add JFE start
import { PurchaseRecordListComponent } from './component/purchase-record-list/purchase-record-list.component';
import { PurchaseRecordListFormComponent } from './component/purchase-record-list/purchase-record-list-form/purchase-record-list-form.component';
import { PurchaseRecordListResultComponent } from './component/purchase-record-list/purchase-record-list-result/purchase-record-list-result.component';
//PRD_0133 #10181 add JFE end
import {
  InventoryShipmentListFormComponent
} from './component/inventory-shipment-list/inventory-shipment-list-form/inventory-shipment-list-form.component';
import {
  InventoryShipmentListResultComponent
} from './component/inventory-shipment-list/inventory-shipment-list-result/inventory-shipment-list-result.component';

import {
  MaterialOrderSubmitConfirmModalComponent
} from './component/material-order/material-order-submit-confirm-modal/material-order-submit-confirm-modal.component';
import { InstructorSystemTypePipe } from './pipe/instructor-system-type.pipe';
import {
  DistributionShipmentListFormComponent
} from './component/distribution-shipment-list/distribution-shipment-list-form/distribution-shipment-list-form.component';
import {
  DistributionShipmentListResultComponent
} from './component/distribution-shipment-list/distribution-shipment-list-result/distribution-shipment-list-result.component';
import {
  SearchMakerReturnProductModalComponent
} from './component/search-maker-return-product-modal/search-maker-return-product-modal.component';
import { MakerReturnListComponent } from './component/maker-return-list/maker-return-list.component';
import { MakerReturnListFormComponent } from './component/maker-return-list/maker-return-list-form/maker-return-list-form.component';
import { MakerReturnListResultComponent } from './component/maker-return-list/maker-return-list-result/maker-return-list-result.component';
import { SearchShopModalComponent } from './component/search-shop-modal/search-shop-modal.component';
import { MakerReturnStatusPipe } from './pipe/maker-return-status.pipe';
//PRD_0137 #10669 mod start
import { MaintSizeComponent } from './component/maint/maint-size/maint-size.component';
//PRD_0137 #10669 mod end
registerLocaleData(localeja, 'ja');
library.add(fas, far);
export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, '/assets/i18n/', '.json?t=' + new Date().getTime());
}

@NgModule({
  declarations: [
    AppComponent,
    ItemComponent,
    FileDropDirective,
    LoginComponent,
    MaintTopComponent,
    HeaderComponent,
    SkuSelectComponent,
    MessageConfirmModalComponent,
    NumberInputPipe,
    NumberInputDirective,
    PartNoInputPipe,
    PartNoInputDirective,
    MonthlyPipe,
    ListComponent,
    ItemListComponent,
    SearchSupplierModalComponent,
    DeliverySubmitConfirmModalComponent,
    SearchStaffModalComponent,
    OrderComponent,
    SearchColorModalComponent,
    SearchKojmstModalComponent,
    SkuInputComponent,
    BreadcrumbComponent,
    DeliveryRequestComponent,
    OrderStatusListComponent,
    DeliveryListViewModalComponent,
    ProductionStatusModalComponent,
    OrderListComponent,
    ItemValidatorDirective,
    OffTimesComponent,
    ProductionStatusValidatorDirective,
    ContactInformationComponent,
    FabricInspectionResultComponent,
    FabricInspectionValidatorDirective,
    TopComponent,
    MenuComponent,
    NewsComponent,
    NewsDetailComponent,
    SafeHtmlPipe,
    IsWithinOpenPeriodPipe,
    IsNewNewsPipe,
    PurchaseStatusPipe,
    //PRD_0133 #10181 add JFE start
    PurchaseRecordStatusPipe,
    //PRD_0133 #10181 add JFE end
    DeliveryPlanComponent,
    DeliveryPlanValidatorDirective,
    DateInputDirective,
    DeliveryPlanSubmitConfirmModalComponent,
    DeliveryPlanValidatorDirective,
    DateInputPipe,
    DeliveryPlanDetailComponent,
    OrderValidatorDirective,
    DeliveryRequestHistoryComponent,
    DeliveryHitoryDateDirective,
    ConstTextAtSpecificValueDirective,
    DeliveryRequestValidatorDirective,
    DelischeComponent,
    MdWeekPipe,
    SortIconComponent,
    DelischeRecordComponent,
    FukukitaruOrderModalComponent,
    FukukitaruOrder01WashComponent,
    SearchCompanyModalComponent,
    FukukitaruOrder01HangTagComponent,
    RatePipe,
    DeliveryCountPipe,
    FukukitaruMasterTextInputComponent,
    FukukitaruOrder01HangTagValidatorDirective,
    FukukitaruSkuInputComponent,
    FukukitaruOrder01WashValidatorDirective,
    AppendicesTermModalComponent,
    AttentionModalComponent,
    FukukitaruInputAssistModalComponent,
    OrderApprovalComponent,
    MaintUserListComponent,
    MaintUserComponent,
    MaintNewsListComponent,
    MaintNewsComponent,
    MaintNewsStatusPipe,
    MaintNewsValidatorDirective,
    BulkRegistItemComponent,
    DeliveryDistributionModalComponent,
    DeliveryDistributionModalValidatorDirective,
    ArticleNumberComponent,
    MisleadingRepresentationListComponent,
    MisleadingApproveComponent,
    MisleadingApproveFormComponent,
    MisleadingRepresentationValidatorDirective,
    LoadingComponent,
    ErrorModalComponent,
    DeliveryListComponent,
    DeriveryStoreComponent,
    DeliveryHeaderComponent,
    DeliveryHeaderErrorMessageComponent,
    DeriveryStoreValidatorDirective,
    ErrorModalComponent,
    MaintCodeComponent,
    CommonValidatorDirective,
    Brand01ValidatorDirective,
    Brand02ValidatorDirective,
    Brand03ValidatorDirective,
    MaterialOrderSubmitConfirmModalComponent,
    PurchaseComponent,
    PurchaseListComponent,
    PurchaseListFormComponent,
    PurchaseListResultComponent,
    //PRD_0133 #10181 add JFE start
    PurchaseRecordListComponent,
    PurchaseRecordListFormComponent,
    PurchaseRecordListResultComponent,
    //PRD_0133 #10181 add JFE end
    ErrorModalComponent,
    FromToCheckDirective,
    FromToCheck2Directive,
    FromToCheck3Directive,
    InventoryShipmentListComponent,
    InventoryShipmentListFormComponent,
    InventoryShipmentListResultComponent,
    FromToCheck3Directive,
    InstructorSystemTypePipe,
    DistributionShipmentListComponent,
    DistributionShipmentListFormComponent,
    DistributionShipmentListResultComponent,
    ShippingInstructionsStatusPipe,
    FromToCheck3Directive,
    MakerReturnComponent,
    SearchMakerReturnProductModalComponent,
    MakerReturnListComponent,
    MakerReturnListFormComponent,
    MakerReturnListResultComponent,
    RequiredRelationDirective,
    RequiredRelation2Directive,
    ArrayRequiredDirective,
    OverBaseNumberDirective,
    FromToCheck3Directive,
    SearchShopModalComponent,
    MakerReturnStatusPipe,
    //PRD_0137 #10669 mod start
    MaintSizeComponent,
    //PRD_0137 #10669 mod end
    // PRD_0141_#10656 add start
    MaintSireComponent,
    MaintSireListComponent,
    MaintSireListFormComponent,
    MaintSireListResultComponent
    // PRD_0141_#10656 add end
  ],
  imports: [
    BrowserModule,
    CommonModule,
    NgbModule.forRoot(),
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
    AppRoutingModule,
    FontAwesomeModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    StorageServiceModule,
    ScrollEventModule
  ],
  entryComponents: [
    SkuSelectComponent,
    SkuInputComponent,
    MessageConfirmModalComponent,
    SearchSupplierModalComponent,
    SearchStaffModalComponent,
    SearchColorModalComponent,
    SearchKojmstModalComponent,
    DeliverySubmitConfirmModalComponent,
    DeliveryPlanSubmitConfirmModalComponent,
    OrderStatusListComponent,
    DeliveryListViewModalComponent,
    ProductionStatusModalComponent,
    OrderListComponent,
    ItemListComponent,
    FukukitaruOrderModalComponent,
    SearchCompanyModalComponent,
    FukukitaruSkuInputComponent,
    AppendicesTermModalComponent,
    AttentionModalComponent,
    DeliveryDistributionModalComponent,
    FukukitaruInputAssistModalComponent,
    ErrorModalComponent,
    SearchMakerReturnProductModalComponent,
    MaterialOrderSubmitConfirmModalComponent,
    SearchShopModalComponent
  ],
  providers: [
    CookieService,
    HeaderService,
    LoadingService,
    SkuInputComponent,
    FukukitaruSkuInputComponent,
    NumberInputPipe,
    PartNoInputPipe,
    DateInputPipe,
    MonthlyPipe,
    SwitchTabService,
    DeliveryCountPipe,
    InstructorSystemTypePipe,
    ShippingInstructionsStatusPipe,
    { provide: HTTP_INTERCEPTORS, useClass: HttpApiInterceptor, multi: true },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
