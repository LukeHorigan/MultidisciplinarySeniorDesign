
/*
  Reading distance from the laser based VL53L1X
  By: Nathan Seidle
  SparkFun Electronics  License: This code is public domain but you buy me a beer if you use this and we meet someday (Beerware license).

  SparkFun labored with love to create this code. Feel like supporting open source hardware?
  Buy a board from SparkFun! https://www.sparkfun.com/products/14667

  This example prints the distance to an object.

  Are you getting weird readings? Be sure the vacuum tape has been removed from the sensor.
  */


#include <Wire.h>
#include "SparkFun_VL53L1X.h" //Click here to get the library: http://librarymanager/All#SparkFun_VL53L1X
#include <WiFi.h>
#include <ComponentObject.h>
#include <RangeSensor.h>
#include <SparkFun_VL53L1X.h>
#include <vl53l1x_class.h>
#include <vl53l1_error_codes.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_ADXL343.h>

#define SHUTDOWN_PIN 2
#define INTERRUPT_PIN 3
#define Module 1
#define ADXL343_SCK 8
#define ADXL343_MISO 4
#define ADXL343_MOSI 9
#define ADXL343_CS 10

int sensor = 2;              // the pin that the sensor is atteched to
int state = LOW;             // by default, no motion detected
int val = 0;                 // variable to store the sensor status (value)
/* Assign a unique ID to this sensor at the same time */
/* Uncomment following line for default Wire bus      */
Adafruit_ADXL343 accel = Adafruit_ADXL343(123);

SFEVL53L1X distanceSensor;
//Uncomment the following line to use the optional shutdown and interrupt pins.
//SFEVL53L1X distanceSensor(Wire, SHUTDOWN_PIN, INTERRUPT_PIN);

//const char* ssid = "WIFIE1734D";
const char* ssid = "RIT-WiFi";
const char* password =  "139HXVHQWLS3Q5GF";
//const char* password =  "rcsolutions";


const uint16_t udpPort = 8888;
const char * udpAddress = "10.115.16.164";

char packetBuffer[255];

//Are we currently connected?
boolean connected = false;
boolean ack = false;
boolean send_stat = true;
boolean start = false;
unsigned long delayStart = 0; // the time the delay started
int packetSize;

//The udp library class
WiFiUDP udp;

typedef struct DataFrame{
  int module;
  int  command;
  int prox;
  int acc;
};

void displayRange(void)
{
  Serial.print  ("Range:         +/- ");

  switch(accel.getRange())
  {
    case ADXL343_RANGE_16_G:
      Serial.print  ("16 ");
      break;
    case ADXL343_RANGE_8_G:
      Serial.print  ("8 ");
      break;
    case ADXL343_RANGE_4_G:
      Serial.print  ("4 ");
      break;
    case ADXL343_RANGE_2_G:
      Serial.print  ("2 ");
      break;
    default:
      Serial.print  ("?? ");
      break;
  }
  Serial.println(" g");
}


void displayDataRate(void)
{
  Serial.print  ("Data Rate:    ");
  digitalWrite(ADXL343_CS,HIGH);
 // Wire.begin(8,9);
  switch(accel.getDataRate())
  {
    case ADXL343_DATARATE_3200_HZ:
      Serial.print  ("3200 ");
      break;
    case ADXL343_DATARATE_1600_HZ:
      Serial.print  ("1600 ");
      break;
    case ADXL343_DATARATE_800_HZ:
      Serial.print  ("800 ");
      break;
    case ADXL343_DATARATE_400_HZ:
      Serial.print  ("400 ");
      break;
    case ADXL343_DATARATE_200_HZ:
      Serial.print  ("200 ");
      break;
    case ADXL343_DATARATE_100_HZ:
      Serial.print  ("100 ");
      break;
    case ADXL343_DATARATE_50_HZ:
      Serial.print  ("50 ");
      break;
    case ADXL343_DATARATE_25_HZ:
      Serial.print  ("25 ");
      break;
    case ADXL343_DATARATE_12_5_HZ:
      Serial.print  ("12.5 ");
      break;
    case ADXL343_DATARATE_6_25HZ:
      Serial.print  ("6.25 ");
      break;
    case ADXL343_DATARATE_3_13_HZ:
      Serial.print  ("3.13 ");
      break;
    case ADXL343_DATARATE_1_56_HZ:
      Serial.print  ("1.56 ");
      break;
    case ADXL343_DATARATE_0_78_HZ:
      Serial.print  ("0.78 ");
      break;
    case ADXL343_DATARATE_0_39_HZ:
      Serial.print  ("0.39 ");
      break;
    case ADXL343_DATARATE_0_20_HZ:
      Serial.print  ("0.20 ");
      break;
    case ADXL343_DATARATE_0_10_HZ:
      Serial.print  ("0.10 ");
      break;
    default:
      Serial.print  ("???? ");
      break;
  }
  Serial.println(" Hz");
}




DataFrame Dataframe;
float pre_x;
float pre_y;
float pre_z;
int sensor_Status;
void setup(){
  // Initilize hardware serial:
  Serial.begin(115200);
  Wire.begin(8,9);
  pinMode(sensor, INPUT);    // initialize PIR sensor Pin as an input
  //Connect to the WiFi network
  connectToWiFi(ssid, password);
  sensor_Status = connectToSensors();
  if(sensor_Status == 0){
     Serial.print("All sensors initialed successfully!");
  }
  Dataframe.module = 2;
  
}

void loop(){

  accel.begin(0x53);
  sensors_event_t event;
  accel.getEvent(&event);
  val = digitalRead(sensor);   // read sensor value
  if (val == HIGH) {           // check if the sensor is HIGH    
    distanceSensor.startRanging(); //Write configuration bytes to initiate measurement
    while (!distanceSensor.checkForDataReady())
    {
      delay(1);
    }
    int distance = distanceSensor.getDistance(); //Get the result of the measurement from the sensor
    distanceSensor.clearInterrupt();
    distanceSensor.stopRanging();
  
    Serial.print("Distance(mm): ");
    Serial.println(distance);
  
    float distanceInches = distance * 0.0393701;
    float distanceFeet = distanceInches / 12.0;
    Dataframe.command = 1;
    udp.beginPacket(udpAddress,udpPort);
    udp.printf("%d%d%f", Dataframe.module, Dataframe.command,distanceFeet);
    udp.endPacket();
  
  }
  
  /* Display the results (acceleration is measured in m/s^2) */
 
  if(abs(event.acceleration.x - pre_x) > 1 || abs(event.acceleration.y - pre_y) > 1 || abs(event.acceleration.z - pre_z) > 1){
        Dataframe.command = 2;
        udp.beginPacket(udpAddress,udpPort);
        udp.printf("%d%d%f", Dataframe.module, Dataframe.command);
        udp.endPacket();
        pre_x = event.acceleration.x;
        pre_y = event.acceleration.y;
        pre_z = event.acceleration.z;
  }
  delay(100);

//   int packetSize = udp.parsePacket();
//   if (packetSize) {
//   IPAddress remoteIp = udp.remoteIP();
//
//     Serial.print(remoteIp);
// 
//     Serial.print(", port ");
// 
//     Serial.println(udp.remotePort());
// 
//     // read the packet into packetBufffer
// 
//     int len = udp.read(packetBuffer, 255);
//     if (len > 0) {
//       packetBuffer[len] = 0; 
//     }
//     Serial.println(packetBuffer);
//     if(!strcmp(packetBuffer,"acknowledge")){
//       ack = true;
//    }
//   }

 // if(ack){
   
//    Dataframe.Data = distanceFeet;
     //only send data when connected
//    if(connected && ack){
//      //Send a packet
//      udp.beginPacket(udpAddress,udpPort);
////      udp.printf("%d %d %d ", Dataframe.module, Dataframe.command, Dataframe.Data);
//      udp.endPacket();
//    //  Dataframe.Data = rand();
//    }

 // }
}

void connectToWiFi(const char * ssid, const char * pwd){
  Serial.println("Connecting to WiFi network: " + String(ssid));

  // delete old config
  WiFi.disconnect(true);
  //register event handler
  WiFi.onEvent(WiFiEvent);
  
  //Initiate connection
  WiFi.begin(ssid);

  Serial.println("Waiting for WIFI connection...");
}

//wifi event handler
void WiFiEvent(WiFiEvent_t event){
    switch(event) {
      case ARDUINO_EVENT_WIFI_STA_GOT_IP:
          //When connected set 
          Serial.print("WiFi connected! IP address: ");
          Serial.println(WiFi.localIP());  
          udp.begin(udpPort);
          connected = true;
          break;
      case ARDUINO_EVENT_WIFI_STA_DISCONNECTED:
          Serial.println("WiFi lost connection");
          connected = false;
          break;
      default: break;
    }
}

int connectToSensors(){
  /* Initialise the sensor */
  if(!accel.begin())
  {
    /* There was a problem detecting the ADXL343 ... check your connections */
    Serial.println("Ooops, no ADXL343 detected ... Check your wiring!");
    Dataframe.acc = -1;
    return -1;
   // while(1);
  }
   accel.setRange(ADXL343_RANGE_16_G);
   displayDataRate();
   displayRange();
  Serial.println("");
  if (distanceSensor.begin() != 0) //Begin returns 0 on a good init
  {
    Serial.println("Sensor failed to begin. Please check wiring. Freezing...");
    Dataframe.prox = -1;
    return -1;
  }
return 0;
}
