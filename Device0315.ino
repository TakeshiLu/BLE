#include <CurieBLE.h>
#include <math.h>

BLEPeripheral blePeripheral; // create peripheral instance
BLEService messageService("19B10010-E8F2-537E-4F6C-D104768A1214"); // create service

BLEUnsignedIntCharacteristic lengthRecieve("19B10012-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);

BLEUnsignedIntCharacteristic recieve1("19B10013-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEUnsignedIntCharacteristic recieve2("19B10014-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEUnsignedIntCharacteristic recieve3("19B10015-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEUnsignedIntCharacteristic recieve4("19B10016-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEUnsignedIntCharacteristic recieve5("19B10017-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);

//BLECharacteristic heartRateChar("19B10017-E8F2-537E-4F6C-D104768A1214", 20, BLERead | BLENotify);

BLEUnsignedIntCharacteristic lengthSend("19B10022-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);

BLEUnsignedIntCharacteristic send1("19B10023-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEUnsignedIntCharacteristic send2("19B10024-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEUnsignedIntCharacteristic send3("19B10025-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEUnsignedIntCharacteristic send4("19B10026-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEUnsignedIntCharacteristic send5("19B10027-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);


void setup() {
  Serial.begin(9600);


  // set the local name peripheral advertises
  blePeripheral.setLocalName("101");
  // set the UUID for the service this peripheral advertises:
  blePeripheral.setAdvertisedServiceUuid(messageService.uuid());

  // add service and characteristics
  blePeripheral.addAttribute(messageService);
  

  blePeripheral.addAttribute(lengthRecieve);
  
  blePeripheral.addAttribute(recieve1);
  blePeripheral.addAttribute(recieve2);
  blePeripheral.addAttribute(recieve3);
  blePeripheral.addAttribute(recieve4);
  blePeripheral.addAttribute(recieve5);
  
  blePeripheral.addAttribute(lengthSend);
  
  blePeripheral.addAttribute(send1);
  blePeripheral.addAttribute(send2);
  blePeripheral.addAttribute(send3);
  blePeripheral.addAttribute(send4);
  blePeripheral.addAttribute(send5);

  lengthRecieve.setValue(0);
  recieve1.setValue(0);
  recieve2.setValue(0);
  recieve3.setValue(0);
  recieve4.setValue(0);
  recieve5.setValue(0);

  lengthSend.setValue(0);
  send1.setValue(0);
  send2.setValue(0);
  send3.setValue(0);
  send4.setValue(0);
  send5.setValue(0);

  

  Serial.println("Bluetooth device active, waiting for connections...");
    // advertise the service
  blePeripheral.begin();

}


void loop() {
  String msg;

  blePeripheral.poll();
 BLECentral central = blePeripheral.central();

  if (central) {
    Serial.print("Connected to central: ");
    // print the central's MAC address:
    Serial.println(central.address());

    while (central.connected()) {
     
      if (lengthRecieve.written()) {
        int lengthR = lengthRecieve.value();
        int msgBlocks = (lengthR/4)+((lengthR%4)+3)/4;
        Serial.print("Message From Central:");
        Serial.println(central.address());
        Serial.print("->");

        if (msgBlocks>0) {
          printReceiveData(recieve1.value());
          if(msgBlocks>1){
            printReceiveData(recieve2.value());
            if(msgBlocks>2){
              printReceiveData(recieve3.value());
              if(msgBlocks>3){
                printReceiveData(recieve4.value());
                if(msgBlocks>4){
                  printReceiveData(recieve5.value());
                }
              }
            }
          }
          Serial.println();
        }
      }
      if (Serial.available() > 0) {
         // 讀取進來的 byte
        msg = Serial.readString();
        if(msg.length()==1){
          sendDemoMessage();
        }else{
          sendMsg(msg);
        }
      }
    }
    // when the central disconnects, print it out:
    Serial.print(F("Disconnected from central: "));
    Serial.println(central.address());


      if (Serial.available() > 0) {
         // 讀取進來的 byte
        msg = Serial.readString();
        if(msg.length()==1){
          sendDemoMessage();
        }else{
          sendMsg(msg);
        }
      }
  }
}
void printReceiveData(int data){
  char inputChar;
//  Serial.println(data);
  for(int i=0;i<4;i++){
    inputChar = (char)data%256;
    data = data/256;
    Serial.print(inputChar);
  }
}
void sendDemoMessage(){
        String msg = "2,hello from device";
          sendMsg(msg);
}
void sendMsg(String msg){
   int msgLen = msg.length();

   int blocks[5] = {0};
   
   for(int i=0;i<msgLen;i++){
            blocks[i/4] = blocks[i/4]+msg[i]*pow (256.0, (double)(i%4));
            //Serial.println(pow (256.0, (double)(i%4)));
          }
          if((msgLen/4)+((msgLen%4)+3)/4>0){
            send1.setValue(blocks[0]);
            if((msgLen/4)+((msgLen%4)+3)/4>1){
              send2.setValue(blocks[1]);
              if((msgLen/4)+((msgLen%4)+3)/4>2){
                send3.setValue(blocks[2]);
                if((msgLen/4)+((msgLen%4)+3)/4>3){
                  send4.setValue(blocks[3]);
                  if((msgLen/4)+((msgLen%4)+3)/4>4){
                   send5.setValue(blocks[4]);
                  }
                }
              }
            }
          }
          Serial.println("send:"+msg);
          lengthSend.setValue(msgLen);
          
}
