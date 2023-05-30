import { GenericList } from 'src/app/model/generic-list';
import { DistributionShipmentSearchResult } from 'src/app/model/distribution-shipment-search-result';
import { CarryType } from 'src/app/const/const';

// tslint:disable:max-line-length
const distributionShipmentSearchResults: DistributionShipmentSearchResult[] = [
  { id: 0, sendStatus: '0', shippingInstructionsAt: new Date(), arrivalFlg: true, arrivalAt: new Date(), deliveryRequestAt: new Date(), orderNumber: 100000, deliveryNumber: '400000', deliveryCount: 1, divisionCode: '11', carryType: CarryType.NORMAL, deliveryLotSum: 1, fixArrivalLotSum: 0, retailPriceSum: 1000, partNo: 'GAZ-00010', productName: 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' },
  { id: 1, sendStatus: '0', shippingInstructionsAt: new Date(), arrivalFlg: true, arrivalAt: new Date(), deliveryRequestAt: new Date(), orderNumber: 100001, deliveryNumber: '400001', deliveryCount: 2, divisionCode: '12', carryType: CarryType.NORMAL, deliveryLotSum: 11, fixArrivalLotSum: 0, retailPriceSum: 11000, partNo: 'GAZ-00020', productName: 'あああああああああああああああああああああああああああああああああああああああ' },
  { id: 2, sendStatus: '0', shippingInstructionsAt: new Date(), arrivalFlg: true, arrivalAt: new Date(), deliveryRequestAt: new Date(), orderNumber: 100002, deliveryNumber: '400002', deliveryCount: 3, divisionCode: '13', carryType: CarryType.NORMAL, deliveryLotSum: 12, fixArrivalLotSum: 0, retailPriceSum: 12000, partNo: 'GAZ-00030', productName: 'ああああああああああ５５５５５５1111111111aaaaaa' },
  { id: 3, sendStatus: '0', shippingInstructionsAt: new Date(), arrivalFlg: true, arrivalAt: new Date(), deliveryRequestAt: new Date(), orderNumber: 100003, deliveryNumber: '400003', deliveryCount: 4, divisionCode: '14', carryType: CarryType.NORMAL, deliveryLotSum: 13, fixArrivalLotSum: 0, retailPriceSum: 13000, partNo: 'GAZ-00040', productName: 'a' },
  { id: 4, sendStatus: '0', shippingInstructionsAt: new Date(), arrivalFlg: true, arrivalAt: new Date(), deliveryRequestAt: new Date(), orderNumber: 100004, deliveryNumber: '400004', deliveryCount: 5, divisionCode: '15', carryType: CarryType.NORMAL, deliveryLotSum: 14, fixArrivalLotSum: 0, retailPriceSum: 14000, partNo: 'GAZ-00050', productName: '品名' },
  { id: 5, sendStatus: '0', shippingInstructionsAt: new Date(), arrivalFlg: true, arrivalAt: new Date(), deliveryRequestAt: new Date(), orderNumber: 100005, deliveryNumber: '400005', deliveryCount: 6, divisionCode: '16', carryType: CarryType.NORMAL, deliveryLotSum: 15, fixArrivalLotSum: 0, retailPriceSum: 15000, partNo: 'GAZ-00060', productName: 'テスト' },
  { id: 6, sendStatus: '0', shippingInstructionsAt: new Date(), arrivalFlg: true, arrivalAt: new Date(), deliveryRequestAt: new Date(), orderNumber: 100006, deliveryNumber: '400006', deliveryCount: 7, divisionCode: '17', carryType: CarryType.NORMAL, deliveryLotSum: 16, fixArrivalLotSum: 0, retailPriceSum: 16000, partNo: 'GAZ-00070', productName: '--------------1111123$$$$$$' },
  { id: 7, sendStatus: '0', shippingInstructionsAt: new Date(), arrivalFlg: true, arrivalAt: new Date(), deliveryRequestAt: new Date(), orderNumber: 100007, deliveryNumber: '400007', deliveryCount: 8, divisionCode: '18', carryType: CarryType.NORMAL, deliveryLotSum: 17, fixArrivalLotSum: 0, retailPriceSum: 17000, partNo: 'GAZ-00080', productName: 'ロペ' },
  { id: 8, sendStatus: '0', shippingInstructionsAt: new Date(), arrivalFlg: true, arrivalAt: new Date(), deliveryRequestAt: new Date(), orderNumber: 100008, deliveryNumber: '400008', deliveryCount: 9, divisionCode: '21', carryType: CarryType.DIRECT, deliveryLotSum: 18, fixArrivalLotSum: 0, retailPriceSum: 18000, partNo: 'GAZ-00090', productName: '信興' },
  { id: 9, sendStatus: '0', shippingInstructionsAt: new Date(), arrivalFlg: true, arrivalAt: new Date(), deliveryRequestAt: new Date(), orderNumber: 100009, deliveryNumber: '400009', deliveryCount: 10, divisionCode: '22', carryType: CarryType.DIRECT, deliveryLotSum: 19, fixArrivalLotSum: 0, retailPriceSum: 9000, partNo: 'GAZ-00100', productName: 'あああああああ' },
  { id: 10, sendStatus: '0', shippingInstructionsAt: new Date(), arrivalFlg: true, arrivalAt: new Date(), deliveryRequestAt: new Date(), orderNumber: 100010, deliveryNumber: '400010', deliveryCount: 11, divisionCode: '11', carryType: CarryType.DIRECT, deliveryLotSum: 9999999999, fixArrivalLotSum: 999999999, retailPriceSum: 9999999999, partNo: 'GAZ-00110', productName: 'っっっっっっｓ' }
];

export const distributionShipmentSearchResultMock = {
  items: distributionShipmentSearchResults,
  nextPageToken: 'aaaaa'
} as GenericList<DistributionShipmentSearchResult>;
