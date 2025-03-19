import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

class Store extends StatefulWidget {
  const Store({super.key});

  @override
  State<Store> createState() => _StoreState();
}

class _StoreState extends State<Store> {
  // 더조은 아카데미 부평캠퍼스 위치
  final LatLng _storeLocation = const LatLng(37.490996, 126.720545);
  GoogleMapController? mapController;
  bool _isMapReady = false;
  Set<Marker> _markers = {};

  @override
  void initState() {
    super.initState();
    print('Store 위젯 초기화');
    _initializeMap();
  }

  void _initializeMap() {
    setState(() {
      _markers = {
        Marker(
          markerId: const MarkerId('store'),
          position: _storeLocation,
          infoWindow: const InfoWindow(
            title: 'THE JOEUN COFFEE',
            snippet: '인천 부평구 경원대로 1366 스테이션타워 7층 707호',
          ),
        ),
      };
      _isMapReady = true;
    });
  }

  // 매장 위치로 이동하는 메소드
  void _moveToStore() {
    mapController?.animateCamera(
      CameraUpdate.newCameraPosition(
        CameraPosition(
          target: _storeLocation,
          zoom: 17.0,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text(
          '매장 안내',
          style: TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.bold,
          ),
        ),
        backgroundColor: const Color(0xFFEE3424),
        foregroundColor: Colors.white,
        actions: [
          // 매장 위치로 이동하는 버튼 추가
          IconButton(
            icon: const Icon(Icons.store),
            onPressed: _moveToStore,
          ),
        ],
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              height: 360,
              width: double.infinity,
              decoration: BoxDecoration(
                border: Border(
                  bottom: BorderSide(color: Colors.grey.shade300),
                ),
              ),
              child: Stack(
                children: [
                  GoogleMap(
                    onMapCreated: (controller) {
                      setState(() {
                        mapController = controller;
                      });
                    },
                    initialCameraPosition: CameraPosition(
                      target: _storeLocation,
                      zoom: 15,
                    ),
                    markers: _markers,
                    myLocationEnabled: true,
                    myLocationButtonEnabled: true,
                    zoomControlsEnabled: true,
                    mapToolbarEnabled: true,
                  ),
                  if (!_isMapReady)
                    Container(
                      child: const Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            CircularProgressIndicator(),
                            SizedBox(height: 16),
                            Text('지도를 불러오는 중...'),
                          ],
                        ),
                      ),
                    ),
                ],
              ),
            ),
            const SizedBox(height: 24),
            
            // 매장 정보
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0),
              child: Container(
                width: double.infinity,
                padding: const EdgeInsets.all(0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // 찾아오시는 길
                    const Text(
                      '찾아오시는 길',
                      style: TextStyle(
                        fontSize: 17,
                        fontWeight: FontWeight.bold,
                        color: Colors.black,
                      ),
                    ),
                    const SizedBox(height: 5),
                    const Text(
                      '인천 부평구 경원대로 1366 스테이션타워 7층 707호',
                      style: TextStyle(
                        fontSize: 15,
                        color: Color(0xFF666666),
                      ),
                    ),
                    const SizedBox(height: 24),
                    
                    // 운영시간
                    const Text(
                      '운영시간',
                      style: TextStyle(
                        fontSize: 17,
                        fontWeight: FontWeight.bold,
                        color: Colors.black,
                      ),
                    ),
                    const SizedBox(height: 5),
                    const Text(
                      '평일 : 09:00 ~ 22:00 (점심시간 13:20 ~ 14:30)\n'
                      '토/일요일 : 09:00 ~ 18:00',
                      style: TextStyle(
                          fontSize: 15,
                          color: Color(0xFF666666),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    mapController?.dispose();
    super.dispose();
  }
}











