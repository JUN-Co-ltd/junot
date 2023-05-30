import { GenericList } from 'src/app/model/generic-list';
import { LgSendType } from 'src/app/const/lg-send-type';
import { InventoryShipmentSearchResult } from 'src/app/model/inventory-shipment-search-result';
import { InstructorSystemType } from 'src/app/const/const';

// tslint:disable:max-line-length
const inventoryShipmentSearchResults: InventoryShipmentSearchResult[] = [
  { cargoAt: new Date(), cargoPlace: '1', instructorSystem: InstructorSystemType.JADORE, brandCode: 'AA', brandName: 'ブランドAAAAAAAAAAAAAAAAAAAAAAAAAAAA', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, divisionCode: '11', deliveryLotSum: 1, retailPriceSum: 1000, partNo: 'AAA-00010', productName: 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' },
  { cargoAt: new Date(), cargoPlace: '1', instructorSystem: InstructorSystemType.JADORE, brandCode: 'AA', brandName: 'ブランドAAAAAAAAAAAAAAAAAAAAAAAAAAAA', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, divisionCode: '12', deliveryLotSum: 11, retailPriceSum: 11000, partNo: 'AAA-00020', productName: 'あああああああああああああああああああああああああああああああああああああああ' },
  { cargoAt: new Date(), cargoPlace: '1', instructorSystem: InstructorSystemType.JADORE, brandCode: 'AA', brandName: 'ブランドAAAAAAAAAAAAAAAAAAAAAAAAAAAA', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, divisionCode: '13', deliveryLotSum: 12, retailPriceSum: 12000, partNo: 'AAA-00030', productName: 'ああああああああああ５５５５５５1111111111aaaaaa' },
  { cargoAt: new Date(), cargoPlace: '1', instructorSystem: InstructorSystemType.JADORE, brandCode: 'GA', brandName: 'GAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGA', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, divisionCode: '14', deliveryLotSum: 13, retailPriceSum: 13000, partNo: 'GAZ-00040', productName: 'a' },
  { cargoAt: new Date(), cargoPlace: '1', instructorSystem: InstructorSystemType.JADORE, brandCode: 'GA', brandName: 'GAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGA', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, divisionCode: '15', deliveryLotSum: 14, retailPriceSum: 14000, partNo: 'GAZ-00050', productName: '品名' },
  { cargoAt: new Date(), cargoPlace: '1', instructorSystem: InstructorSystemType.JADORE, brandCode: 'GA', brandName: 'GAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGA', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, divisionCode: '16', deliveryLotSum: 15, retailPriceSum: 15000, partNo: 'GAZ-00060', productName: 'テスト' },
  { cargoAt: new Date(), cargoPlace: '1', instructorSystem: InstructorSystemType.JADORE, brandCode: 'GA', brandName: 'GAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGAGA', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, divisionCode: '17', deliveryLotSum: 16, retailPriceSum: 16000, partNo: 'GAZ-00070', productName: '--------------1111123$$$$$$' },
  { cargoAt: new Date(), cargoPlace: '1', instructorSystem: InstructorSystemType.JADORE, brandCode: 'GI', brandName: 'GI11111111111111111111111111111111111111111111', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, divisionCode: '18', deliveryLotSum: 17, retailPriceSum: 17000, partNo: 'GII-00080', productName: 'ロペ' },
  { cargoAt: new Date(), cargoPlace: '1', instructorSystem: InstructorSystemType.JADORE, brandCode: 'GI', brandName: 'GI11111111111111111111111111111111111111111111', lgSendType: LgSendType.INSTRUCTION as LgSendType, divisionCode: '21', deliveryLotSum: 18, retailPriceSum: 18000, partNo: 'GII-00090', productName: '信興' },
  { cargoAt: new Date(), cargoPlace: '1', instructorSystem: InstructorSystemType.JADORE, brandCode: 'GI', brandName: 'GI11111111111111111111111111111111111111111111', lgSendType: LgSendType.INSTRUCTION as LgSendType, divisionCode: '22', deliveryLotSum: 19, retailPriceSum: 9000, partNo: 'GII-00100', productName: 'あああああああ' },
  { cargoAt: new Date(), cargoPlace: '1', instructorSystem: InstructorSystemType.JADORE, brandCode: 'GI', brandName: 'GI11111111111111111111111111111111111111111111', lgSendType: LgSendType.INSTRUCTION as LgSendType, divisionCode: '11', deliveryLotSum: 9999999999, retailPriceSum: 9999999999, partNo: 'GII-00110', productName: 'っっっっっっｓ' }
];

export const InventoryShipmentSearchResultMock = {
  items: inventoryShipmentSearchResults,
  nextPageToken: 'aaaaa'
} as GenericList<InventoryShipmentSearchResult>;
