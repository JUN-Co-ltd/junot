import { ColorSize } from 'src/app/model/color-size';
import { DeliverySku } from 'src/app/model/delivery-sku';
import { DeliveryDetail } from 'src/app/model/delivery-detail';

export const SKUS = [
  { colorCode: '22', size: 'S' },
  { colorCode: '02', size: 'L' },
  { colorCode: '00', size: '23.5' },
  { colorCode: '02', size: 'S' },
  { colorCode: '00', size: '23.5' },
  { colorCode: '22', size: '24' },
  { colorCode: '01', size: 'LL' },
  { colorCode: '06', size: '23.0' }
] as ColorSize[];

const onlyLotDeliverySkus = [
  { deliveryLot: 1 },
  { deliveryLot: 2 },
  { deliveryLot: 3 },
  { deliveryLot: 4 },
  { deliveryLot: 5 },
  { deliveryLot: 6 },
  { deliveryLot: 7 },
  { deliveryLot: 8 },
  { deliveryLot: 9 },
  { deliveryLot: 10 }
] as DeliverySku[];

export const onlyLotDeliveryDetailMock = { deliverySkus: onlyLotDeliverySkus } as DeliveryDetail;

export const deliverySkus1 = [
  { colorCode: '01', size: 'S', deliveryLot: 1 },
  { colorCode: '01', size: 'L', deliveryLot: 11 },
  { colorCode: '01', size: '23.5', deliveryLot: 111 },
  { colorCode: '02', size: 'S', deliveryLot: 1111 },
  { colorCode: '02', size: '23.5', deliveryLot: 10 },
  { colorCode: '02', size: '24', deliveryLot: 100 },
  { colorCode: '03', size: 'LL', deliveryLot: 1000 },
  { colorCode: '03', size: '23.0', deliveryLot: 12 }
] as DeliverySku[];
export const deliverySkus2 = [
  { colorCode: '01', size: 'S', deliveryLot: 2 },
  { colorCode: '01', size: 'L', deliveryLot: 22 },
  { colorCode: '01', size: '23.5', deliveryLot: 222 },
  { colorCode: '02', size: 'S', deliveryLot: 2222 },
  { colorCode: '02', size: '24', deliveryLot: 200 },
  { colorCode: '03', size: 'LL', deliveryLot: 2000 },
  { colorCode: '03', size: '23.0', deliveryLot: 23 }
] as DeliverySku[];
export const deliverySkus3 = [
  { colorCode: '01', size: 'S', deliveryLot: 3 },
  { colorCode: '01', size: 'L', deliveryLot: 33 },
  { colorCode: '01', size: '23.5', deliveryLot: 333 },
  { colorCode: '02', size: 'S', deliveryLot: 3333 },
  { colorCode: '02', size: '23.5', deliveryLot: 30 },
  { colorCode: '02', size: '24', deliveryLot: 300 },
  { colorCode: '03', size: 'LL', deliveryLot: 3000 },
  { colorCode: '03', size: '23.0', deliveryLot: 34 }
] as DeliverySku[];
export const deliveryDetailsMock = [
  { divisionCode: '11', deliverySkus: deliverySkus1 },
  { divisionCode: '12', deliverySkus: deliverySkus2 },
  { divisionCode: '13', deliverySkus: deliverySkus3 },
] as DeliveryDetail[];
