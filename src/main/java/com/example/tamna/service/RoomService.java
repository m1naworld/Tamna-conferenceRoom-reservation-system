package com.example.tamna.service;

import com.example.tamna.model.Room;
import com.example.tamna.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomMapper roomMapper;

    public List<Room> roomList(){
        return roomMapper.AllFindRoom();
    }


    public List<Room> getFloorRoom(int floor){
        return roomMapper.findFloorRoom(floor);
    }

    public Room getRoomId(int roomId){
        return roomMapper.findRoomId(roomId);
    }


}
