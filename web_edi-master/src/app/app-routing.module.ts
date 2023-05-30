import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { AuthGuard } from './guard/auth.guard';
import { AuthUtils } from './util/auth-utils';

import { ItemComponent } from './component/item/item.component';
import { LoginComponent } from './component/login/login.component';
import { OrderComponent } from './component/order/order.component';
import { OrderApprovalComponent } from './component/order-approval/order-approval.component';
import { DeliveryPlanComponent } from './component/delivery-plan/delivery-plan.component';
import { DeliveryRequestComponent } from './component/delivery-request/delivery-request.component';
import { ListComponent } from './component/list/list.component';
import { OffTimesComponent } from './component/off-times/off-times.component';
import { FabricInspectionResultComponent } from './component/fabric-inspection-result/fabric-inspection-result.component';
import { TopComponent } from './component/top/top.component';
import { NewsDetailComponent } from './component/news-detail/news-detail.component';
import { DeliveryPlanDetailComponent } from './component/delivery-plan-detail/delivery-plan-detail.component';
import { DelischeComponent } from './component/delische/delische.component';
import { FukukitaruOrder01WashComponent } from './component/fukukitaru-order01-wash/fukukitaru-order01-wash.component';
import { FukukitaruOrder01HangTagComponent } from './component/fukukitaru-order01-hang-tag/fukukitaru-order01-hang-tag.component';
import { DeliveryListComponent } from './component/delivery-list/delivery-list.component';
import { DeriveryStoreComponent } from './component/derivery-store/derivery-store.component';

import { MaintTopComponent } from 'src/app/component/maint/maint-top/maint-top.component';
import { MaintUserListComponent } from 'src/app/component/maint/maint-user-list/maint-user-list.component';
import { MaintUserComponent } from 'src/app/component/maint/maint-user/maint-user.component';
// PRD_0141 #10656 add JFE start
import { MaintSireListComponent } from 'src/app/component/maint/maint-sire-list/maint-sire-list.component';
import { MaintSireComponent } from 'src/app/component/maint/maint-sire/maint-sire.component';
// PRD_0141 #10656 add JFE end
import { MaintNewsListComponent } from 'src/app/component/maint/maint-news-list/maint-news-list.component';
import { MaintNewsComponent } from 'src/app/component/maint/maint-news/maint-news.component';
import { MaintCodeComponent } from 'src/app/component/maint/maint-code/maint-code.component';
//PRD_0137 #10669 mod start
import { MaintSizeComponent } from './component/maint/maint-size/maint-size.component';
//PRD_0137 #10669 mod end
import { PurchaseListComponent } from 'src/app/component/purchase-list/purchase-list.component';
import { BulkRegistItemComponent } from './component/bulk-regist-item/bulk-regist-item.component';
import { PurchaseComponent } from './component/purchase/purchase.component';
//PRD_0133 #10181 add JFE start
import { PurchaseRecordListComponent } from './component/purchase-record-list/purchase-record-list.component';
//PRD_0133 #10181 add JFE end
import { MakerReturnComponent } from './component/maker-return/maker-return.component';

import { MisleadingApproveComponent } from 'src/app/component/misleading-approve/misleading-approve.component';

import {
  MisleadingRepresentationListComponent
} from 'src/app/component/misleading-representation-list/misleading-representation-list.component';
import { DistributionShipmentListComponent } from './component/distribution-shipment-list/distribution-shipment-list.component';
import { MakerReturnListComponent } from './component/maker-return-list/maker-return-list.component';
// PRD_0061 del SIT start
//import { InventoryShipmentListComponent } from './component/inventory-shipment-list/inventory-shipment-list.component';
// PRD_0061 del SIT end

const routes: Routes = [
  { path: '', component: LoginComponent },
  {
    path: 'top', data: { breadcrumb: 'トップ' },
    children: [
      { path: '', component: TopComponent, canActivate: [AuthGuard], data: { breadcrumb: null } },
      { path: 'news/:id', component: NewsDetailComponent, canActivate: [AuthGuard], data: { breadcrumb: 'お知らせ' } }
    ]
  },
  {
    path: 'items', data: { breadcrumb: '一覧' },
    children: [
      { path: '', component: ListComponent, canActivate: [AuthGuard], data: { breadcrumb: null } },
      { path: 'new', component: ItemComponent, canActivate: [AuthGuard], data: { breadcrumb: '商品登録' } },
      { path: ':id/edit', component: ItemComponent, canActivate: [AuthGuard], data: { breadcrumb: '品番編集' } }
    ]
  },
  {
    path: 'orders', data: { breadcrumb: '一覧' },
    children: [
      { path: '', component: ListComponent, canActivate: [AuthGuard], data: { breadcrumb: null } },
      { path: 'new', component: OrderComponent, canActivate: [AuthGuard], data: { breadcrumb: '受注登録' } },
      { path: ':id/edit', component: OrderComponent, canActivate: [AuthGuard], data: { breadcrumb: '発注編集' } },
      { path: ':id/view', component: OrderComponent, canActivate: [AuthGuard], data: { breadcrumb: '発注参照' } },
      {
        path: ':id/approval', component: OrderApprovalComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '発注承認', authFn: AuthUtils.isJun }
      },
      {
        path: 'misleadingRepresentations/:id/edit', component: MisleadingApproveComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '優良誤認検査', authFn: AuthUtils.isJun }
      }
    ]
  },
  {
    path: 'deliveryPlans', data: { breadcrumb: '一覧' },
    children: [
      { path: '', component: ListComponent, canActivate: [AuthGuard], data: { breadcrumb: null } },
      { path: 'new', component: DeliveryPlanComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品予定登録' } },
      { path: ':id/edit', component: DeliveryPlanComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品予定編集' } },
      { path: ':id/view', component: DeliveryPlanComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品予定参照' } },
      {
        path: 'show/:orderId', component: DeliveryPlanDetailComponent, canActivate: [AuthGuard],  // idは発注ID
        data: { breadcrumb: '納品予定明細', visible: false }
      }
    ]
  },
  {
    path: 'deliveries', data: { breadcrumb: '一覧' },
    children: [
      { path: '', component: ListComponent, canActivate: [AuthGuard], data: { breadcrumb: null } },
      { path: 'new', component: DeliveryRequestComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品依頼登録' } },
      { path: ':id/edit', component: DeliveryRequestComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品依頼編集' } },
      { path: ':id/view', component: DeliveryRequestComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品依頼参照' } },
      { path: ':id/correct', component: DeliveryRequestComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品依頼訂正' } },
      { path: 'deriveryStores/new', component: DeriveryStoreComponent, canActivate: [AuthGuard], data: { breadcrumb: '店舗配分登録' } },
      { path: 'deriveryStores/:id/edit', component: DeriveryStoreComponent, canActivate: [AuthGuard], data: { breadcrumb: '店舗配分編集' } },
      { path: 'deriveryStores/:id/view', component: DeriveryStoreComponent, canActivate: [AuthGuard], data: { breadcrumb: '店舗配分参照' } },
      { path: 'deriveryStores/:id/correct', component: DeriveryStoreComponent, canActivate: [AuthGuard], data: { breadcrumb: '店舗配分訂正' } }
    ]
  },
  {
    path: 'deliveryStores', data: { breadcrumb: '一覧' },
    children: [
      { path: '', component: ListComponent, canActivate: [AuthGuard], data: { breadcrumb: null, authFn: AuthUtils.isJun } },
      {
        path: 'new', component: DeriveryStoreComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '店舗配分登録', authFn: AuthUtils.isJun }
      },
      {
        path: ':id/edit', component: DeriveryStoreComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '店舗配分編集', authFn: AuthUtils.isJun }
      },
      {
        path: ':id/view', component: DeriveryStoreComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '店舗配分参照', authFn: AuthUtils.isJun }
      },
      {
        path: ':id/correct', component: DeriveryStoreComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '店舗配分訂正', authFn: AuthUtils.isJun }
      }
    ]
  },
  {
    path: 'fabricInspectionResults', data: { breadcrumb: '一覧' },
    children: [
      { path: '', component: ListComponent, canActivate: [AuthGuard], data: { breadcrumb: null } },
      { path: 'new', component: FabricInspectionResultComponent, canActivate: [AuthGuard], data: { breadcrumb: '生地検査結果登録' } }
    ]
  },
  {
    path: 'delische', data: { breadcrumb: 'WEBデリスケ' },
    children: [
      {
        path: '', component: DelischeComponent, canActivate: [AuthGuard],
        data: { breadcrumb: null, authFn: AuthUtils.isJun }
      },
      { path: 'deliveries/new', component: DeliveryRequestComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品依頼登録' } },
      { path: 'deliveries/:id/edit', component: DeliveryRequestComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品依頼編集' } },
      { path: 'deliveries/:id/view', component: DeliveryRequestComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品依頼参照' } },
      { path: 'deliveries/:id/correct', component: DeliveryRequestComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品依頼訂正' } },
      {
        path: 'deliveryStores/new', component: DeriveryStoreComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '店舗配分登録', authFn: AuthUtils.isJun }
      },
      {
        path: 'deliveryStores/:id/edit', component: DeriveryStoreComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '店舗配分編集', authFn: AuthUtils.isJun }
      },
      {
        path: 'deliveryStores/:id/view', component: DeriveryStoreComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '店舗配分参照', authFn: AuthUtils.isJun }
      },
      {
        path: 'deliveryStores/:id/correct', component: DeriveryStoreComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '店舗配分訂正', authFn: AuthUtils.isJun }
      }
    ]
  },
  {
    path: 'deliverySearchList', data: { breadcrumb: '配分一覧' },
    children: [
      { path: '', component: DeliveryListComponent, canActivate: [AuthGuard], data: { breadcrumb: null } },
      { path: 'deliveries/new', component: DeliveryRequestComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品依頼登録' } },
      { path: 'deliveries/:id/edit', component: DeliveryRequestComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品依頼編集' } },
      { path: 'deliveries/:id/view', component: DeliveryRequestComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品依頼参照' } },
      { path: 'deliveries/:id/correct', component: DeliveryRequestComponent, canActivate: [AuthGuard], data: { breadcrumb: '納品依頼訂正' } },
      {
        path: 'deliveryStores/new', component: DeriveryStoreComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '店舗配分登録', authFn: AuthUtils.isJun }
      },
      {
        path: 'deliveryStores/:id/edit', component: DeriveryStoreComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '店舗配分編集', authFn: AuthUtils.isJun }
      },
      {
        path: 'deliveryStores/:id/view', component: DeriveryStoreComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '店舗配分参照', authFn: AuthUtils.isJun }
      },
      {
        path: 'deliveryStores/:id/correct', component: DeriveryStoreComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '店舗配分訂正', authFn: AuthUtils.isJun }
      }
    ]
  },
  {
    path: 'fukukitaruOrder01Wash', data: { breadcrumb: '一覧' },
    children: [
      { path: '', component: ListComponent, canActivate: [AuthGuard], data: { breadcrumb: null } },
      { path: 'new', component: FukukitaruOrder01WashComponent, canActivate: [AuthGuard], data: { breadcrumb: '資材情報登録(洗濯ネーム)' } },
      { path: ':id/edit', component: FukukitaruOrder01WashComponent, canActivate: [AuthGuard], data: { breadcrumb: '資材情報更新(洗濯ネーム)' } },
      { path: ':id/view', component: FukukitaruOrder01WashComponent, canActivate: [AuthGuard], data: { breadcrumb: '資材情報参照(洗濯ネーム)' } }
    ]
  },
  {
    path: 'fukukitaruOrder01HangTag', data: { breadcrumb: '一覧' },
    children: [
      { path: '', component: ListComponent, canActivate: [AuthGuard], data: { breadcrumb: null } },
      { path: 'new', component: FukukitaruOrder01HangTagComponent, canActivate: [AuthGuard], data: { breadcrumb: '資材情報登録(下札)' } },
      { path: ':id/edit', component: FukukitaruOrder01HangTagComponent, canActivate: [AuthGuard], data: { breadcrumb: '資材情報更新(下札)' } },
      { path: ':id/view', component: FukukitaruOrder01HangTagComponent, canActivate: [AuthGuard], data: { breadcrumb: '資材情報参照(下札)' } }
    ]
  },
  {
    path: 'maint', data: { breadcrumb: 'マスタメンテナンス' },
    children: [
      {
        path: '', component: MaintTopComponent, canActivate: [AuthGuard],
        data: { breadcrumb: null, authFn: AuthUtils.isJun }
      },
      {
        path: 'users', data: { breadcrumb: 'ユーザ一覧' },
        children: [
          {
            path: '', component: MaintUserListComponent, canActivate: [AuthGuard],
            data: { breadcrumb: null, authFn: AuthUtils.isMasterMaintenance }
          },
          {
            path: 'new', component: MaintUserComponent, canActivate: [AuthGuard],
            data: { breadcrumb: 'ユーザ登録', authFn: AuthUtils.isMasterMaintenance }
          },
          {
            path: ':id/edit', component: MaintUserComponent, canActivate: [AuthGuard],
            data: { breadcrumb: 'ユーザ編集', authFn: AuthUtils.isMasterMaintenance }
          }
        ]
      },
      {
      // PRD_0141 #10656 add start
        path: 'sires', data: { breadcrumb: '取引先一覧' },
        children: [
          {
            path: '', component: MaintSireListComponent, canActivate: [AuthGuard],
            data: { breadcrumb: null, authFn: AuthUtils.isMasterMaintenance }
          },
          {
            path: 'new', component: MaintSireComponent, canActivate: [AuthGuard],
            data: { breadcrumb: '取引先登録', authFn: AuthUtils.isMasterMaintenance }
          },
          {
            path: ':sireCode/edit', component: MaintSireComponent, canActivate: [AuthGuard],
            data: { breadcrumb: '取引先編集', authFn: AuthUtils.isMasterMaintenance }
          }

        ]
      },
      {
        // PRD_0141 #10656 add end
        path: 'news', data: { breadcrumb: 'お知らせ一覧' },
        children: [
          {
            path: '', component: MaintNewsListComponent, canActivate: [AuthGuard],
            data: { breadcrumb: null, authFn: AuthUtils.isJun }
          },
          {
            path: 'new', component: MaintNewsComponent, canActivate: [AuthGuard],
            data: { breadcrumb: 'お知らせ登録', authFn: AuthUtils.isJun }
          },
          {
            path: ':id/edit', component: MaintNewsComponent, canActivate: [AuthGuard],
            data: { breadcrumb: 'お知らせ編集', authFn: AuthUtils.isJun }
          }
        ]
      },
      {
        path: 'maint-code', data: { breadcrumb: 'コード管理' },
        children: [
          {
            path: '', component: MaintCodeComponent, canActivate: [AuthGuard],
            data: { breadcrumb: null, authFn: AuthUtils.isMasterMaintenance }
          }
        ]
      //PRD_0137 #10669 mod start
      },
      {
        path: 'maint-size', data: { breadcrumb: 'サイズ登録' },
        children: [
          {
            path: '', component: MaintSizeComponent, canActivate: [AuthGuard],
            data: { breadcrumb: null, authFn: AuthUtils.isMasterMaintenance }
          }
        ]
      }
      //PRD_0137 #10669 mod end
    ]
  },
  {
    path: 'bulkRegistItems', data: { breadcrumb: '商品・品番一括登録' },
    children: [
      {
        path: '', component: BulkRegistItemComponent, canActivate: [AuthGuard],
        data: { breadcrumb: null, authFn: AuthUtils.isEdi }
      }
    ]
  },
  {
    path: 'misleadingRepresentations', data: { breadcrumb: '優良誤認一覧' },
    children: [
      {
        path: '', component: MisleadingRepresentationListComponent, canActivate: [AuthGuard],
        data: { breadcrumb: null, authFn: AuthUtils.isJun }
      },
      {
        path: ':id/edit', component: MisleadingApproveComponent, canActivate: [AuthGuard],
        data: { breadcrumb: '優良誤認検査', authFn: AuthUtils.isJun }
      }
    ]
  },
  {
    path: 'purchases', data: { breadcrumb: '仕入一覧', authFn: AuthUtils.isDista }, canActivate: [AuthGuard],
    children: [
      { path: '', component: PurchaseListComponent, data: { breadcrumb: null } },
      { path: ':id/new', component: PurchaseComponent, data: { breadcrumb: '一括仕入登録' } },
      { path: ':id/edit', component: PurchaseComponent, data: { breadcrumb: '一括仕入編集' } },
      { path: ':id/view', component: PurchaseComponent, data: { breadcrumb: '一括仕入参照' } }
    ]
  },
  //PRD_0133 #10181 add JFE start
  {
    path: 'purchasesRecord', data: { breadcrumb: '仕入実績一覧', authFn: AuthUtils.isJun }, canActivate: [AuthGuard],
    children: [
      { path: '', component: PurchaseRecordListComponent, data: { breadcrumb: null } }
    ]
  },
  //PRD_0133 #10181 add JFE end
  {
    path: 'distributionShipments', data: { breadcrumb: '配分出荷指示一覧', authFn: AuthUtils.isDista },
    children: [
      {
        path: '', component: DistributionShipmentListComponent, canActivate: [AuthGuard],
        data: { breadcrumb: null }
      }
    ]
  },
  {
    path: 'makerReturns', data: { breadcrumb: 'メーカー返品一覧', authFn: AuthUtils.isMakerReturn }, canActivate: [AuthGuard],
    children: [
      { path: '', component: MakerReturnListComponent, data: { breadcrumb: null } },
      { path: 'new', component: MakerReturnComponent, data: { breadcrumb: 'メーカー返品登録' } },
      { path: ':voucherNumber/edit', component: MakerReturnComponent, data: { breadcrumb: 'メーカー返品編集' } },
      { path: ':voucherNumber/view', component: MakerReturnComponent, data: { breadcrumb: 'メーカー返品参照' } }
    ]
  },
  // PRD_0061 del SIT start
  /*{
    path: 'inventoryShipment', data: { breadcrumb: '在庫出荷一覧', authFn: AuthUtils.isDista },
    children: [
      {
        path: '', component: InventoryShipmentListComponent, canActivate: [AuthGuard],
        data: { breadcrumb: null }
      },
    ]
  },
  */
  // PRD_0061 del SIT end
  { path: 'login', component: LoginComponent },
  { path: 'maintenance', component: OffTimesComponent },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forRoot(routes),
  ],
  exports: [RouterModule],
  declarations: []
})
export class AppRoutingModule { }
