package com.example.tamna.service;

import com.example.tamna.dto.CancelDto;
import com.example.tamna.dto.DetailBookingDataDto;
import com.example.tamna.model.Booking;
import com.example.tamna.mapper.BookingMapper;
import com.example.tamna.mapper.ParticipantsMapper;
import com.example.tamna.model.JoinBooking;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingMapper bookingMapper;
    private final ParticipantsMapper participantsMapper;
    private final UserService userService;


    public Date time() {
        final long miliseconds = System.currentTimeMillis();
        return new Date(miliseconds);
    }

    // 전체 회의실 예약 현황 가져오기
    public List<Booking> allRoomBookingState() {
        Date today = time();
        return bookingMapper.findAllRoomState(today);
    }

    // 회의실별 예약현황
    public List<Booking> roomBookingState(int roomId) {

        Date today = time();
        return bookingMapper.findByRoomId(today, roomId);
    }

    // 층수 별 예약 현황
    public List<Booking> floorBookingData(int floor) {
        Date today = time();
        return bookingMapper.findByFloor(today, floor);
    }

    // 현재 회의실 예약 되어 있는지 확인
    public boolean findSameBooking(int roomId, String startTime, String endTime) {
        Date today = time();
        List<Integer> sameBooking = bookingMapper.findSameBooking(today, roomId, startTime, endTime);
        System.out.println(sameBooking);
        if (!sameBooking.isEmpty()) {
            System.out.println("현재 예약된 회의실!");
            return true;
        }
        return false;
    }


    // 예약된 회의실 디테일 정보
    public List<DetailBookingDataDto> floorDetailBookingData(int floor) {
        Date today = time();
        List<DetailBookingDataDto> floorDetailBooking = new ArrayList<>();
        List<Booking> floorBooking = bookingMapper.findByFloor(today, floor);

        for (int k = 0; k < floorBooking.toArray().length; k++) {
            int roomId = floorBooking.get(k).getRoomId();
            String startTime = floorBooking.get(k).getStartTime();
            List<JoinBooking> detailData = bookingMapper.findDetailBookingData(today, roomId, startTime);
            DetailBookingDataDto combineData = new DetailBookingDataDto();

            if (!detailData.isEmpty()) {
                Map<String, String> applicants = new HashMap<>();
                List<String> teamMate = new ArrayList<>();
                for (int i = 0; i < detailData.toArray().length; i++) {
                    if (detailData.get(i).isUserType()) {
                        applicants.put("userId", detailData.get(i).getUserId());
                        applicants.put("userName", detailData.get(i).getUserName());
                        combineData.setApplicant(applicants);
                    } else {
                        teamMate.add(detailData.get(i).getUserName());
                    }
                }
                combineData.setBookingId(detailData.get(0).getBookingId());
                combineData.setRoomId(detailData.get(0).getRoomId());
                combineData.setRoomName(detailData.get(0).getRoomName());
                combineData.setStartTime(detailData.get(0).getStartTime());
                combineData.setEndTime(detailData.get(0).getEndTime());
                combineData.setRoomType(detailData.get(0).getRoomType());
                combineData.setOfficial(detailData.get(0).isOfficial());
                combineData.setParticipants(teamMate);

                floorDetailBooking.add(combineData);
            }
        }
        return floorDetailBooking;

    }

    // 회의실 예약 및 동시 예약 불가 처리
    public int insertBooking(int roomId, String startTime, String endTime, boolean official) {
        Date today = time();

        bookingMapper.insertBooking(today, roomId, startTime, endTime, official);
        List<Integer> bookingId = bookingMapper.selectResultInsert(today, roomId, startTime, endTime);
        int minBookingId = Collections.min(bookingId);
        if (bookingId.toArray().length != 1) {
            for (int i : bookingId) {
                if (minBookingId != i) {
                    bookingMapper.deleteBooking(i);
                    participantsMapper.deleteParticipants(i);
                }
            }
        }
        return minBookingId;
    }

    // bookingId들 한번에 검색하기 위한
    public String addBookingId(Collection<Integer> bookingIdList) {
        if (!bookingIdList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            bookingIdList.forEach(m -> sb.append("'" + m + "',"));
            return sb.substring(0, sb.length() - 1);
        } else {
            return null;
        }
    }

    // 내가 포함된 에약 데이터 조회
    public List<DetailBookingDataDto> userIncludedBooking(String userId) {
        Date today = time();
        List<DetailBookingDataDto> allMyBookingData = new ArrayList<>();

        // 내가 예약된 예약정보의 bookingId들 받아옴
        List<Integer> bookingIdList = bookingMapper.findMyBookingId(today, userId);
        String bookingIdString = addBookingId(bookingIdList);

        // 예약한 데이터가 있을 때
        if (bookingIdString != null) {
            // bookingId들 문자열로 변환 후 한번에 데이터들 모두 조회
            List<JoinBooking> myBookingList = bookingMapper.findMyBookingData(bookingIdString);

            if (!myBookingList.isEmpty()) {
                for (int i : bookingIdList) {
                    DetailBookingDataDto combineData = new DetailBookingDataDto();
                    List<String> teamMate = new ArrayList<>();
                    for (int j = 0; j < myBookingList.toArray().length; j++) {
                        if (myBookingList.get(j).getBookingId() == i) {
                            if (myBookingList.get(j).isUserType()) {
                                combineData.setBookingId(myBookingList.get(j).getBookingId());
                                combineData.setRoomId(myBookingList.get(j).getRoomId());
                                combineData.setRoomName(myBookingList.get(j).getRoomName());
                                combineData.setRoomType(myBookingList.get(j).getRoomType());
                                combineData.setStartTime(myBookingList.get(j).getStartTime());
                                combineData.setEndTime(myBookingList.get(j).getEndTime());
                                combineData.setOfficial(myBookingList.get(j).isOfficial());
                                combineData.setMode(myBookingList.get(j).getMode());
                                Map<String, String> applicant = new HashMap<>();
                                applicant.put("userId", myBookingList.get(j).getUserId());
                                applicant.put("userName", myBookingList.get(j).getUserName());
                                combineData.setApplicant(applicant);
                            } else {
                                teamMate.add(myBookingList.get(j).getUserName());
                            }
                        } // bookingId가 다를 때 최상위 for문으로 이동
                        continue;
                    }
                    combineData.setParticipants(teamMate);
                    allMyBookingData.add(combineData);
                }
            } else {
                allMyBookingData.add(null);
            }
        }
        return allMyBookingData;
    }

    ;

    // bookingId를 통한 공식일정 검색
    public CancelDto selectBookingId(int bookingId, String userId) {
        CancelDto result =  bookingMapper.selectOfficial(bookingId);

        if(result.getUserId().equals(userId)){
            return result;
        }
        return null;
    }

    // 공식일정인지 결과 확인
    public List<Boolean> checkOfficial(int roomId, String startTime, String endTime) {
        Date today = time();
        return bookingMapper.findSameTimeOfficial(today, roomId, startTime, endTime);
    }

    // 예약 취소
    public String deleteBooking(int bookingId) {
        int checkBookingDelete = bookingMapper.deleteBooking(bookingId);
        int checkParticipantsDelete = participantsMapper.deleteParticipants(bookingId);
        if (checkBookingDelete == 1 && checkParticipantsDelete >= 1) {
            return "success";
        }
        return "fail";
    }


    // 현재 시간
    public int nowHour() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        return hour;
    }

    // 공식 일정 취소시 인재분들 예약 살리기
    public String deleteOfficialBooking(CancelDto booking) {
        Date today = time();
        int hour = nowHour();
        List<CancelDto> canceledBooking = bookingMapper.selectCancelBooking(today, booking.getRoomId(), booking.getStartTime(), booking.getEndTime());

        if (!canceledBooking.isEmpty()) {
            // 삭제하고 남은 취소되었던 예약 데이터
            List<CancelDto> newCanceledBooking = new ArrayList<CancelDto>();
            Map<String, Integer> applicantMap = new HashMap<>();
            Set<Integer> deleteBookingId = new HashSet<>();
            for (int i = 0; i < canceledBooking.toArray().length; i++) {
                int bookingStartTime = Integer.parseInt(canceledBooking.get(i).getStartTime().substring(0,2));
                // 현재시간 보다 + 1시간 보다 종료시간이 작은 예약들 삭제
                if (bookingStartTime <= hour + 1) {
                    deleteBookingId.add(canceledBooking.get(i).getBookingId());
                } else {
                    newCanceledBooking.add(canceledBooking.get(i));
                    if (canceledBooking.get(i).getUserType()) {
                        applicantMap.put(canceledBooking.get(i).getUserId(), canceledBooking.get(i).getBookingId());
                    }
                }
            }

            String bookingsIdString = addBookingId(deleteBookingId);
            if(bookingsIdString != null) {
                int deleteCount1 = bookingMapper.deleteBookings(bookingsIdString);
                int deleteCount2 = participantsMapper.deleteSeveralParticipants(bookingsIdString);
            }

            if (!newCanceledBooking.isEmpty()) {
                // 검색을 위한 유저아이디 값 모음
                Set<String> applicantsSet = new HashSet<>(applicantMap.keySet());
                String applicantsString = userService.changeString(null, applicantsSet);

                // cancel되었던 bookingId 리스트 <- 동시간대 예약자를 확인하기 위한 for 문에 쓰임
                List<Integer> allBookingId = new ArrayList<>(applicantMap.values());

                // 취소된 유저들의 다른 회의실 예약 현황 조회후 삭제
                List<String> otherBookingUserId = bookingMapper.findNotCancelMyBooking(today, applicantsString);

                List<CancelDto> restCanceledBooking = new ArrayList<CancelDto>(); // 참여자 동시간대 cancel 예약 취소를 위한 삭제한 예약들을 제외한 새로운 canceldbooking리스트

                if (!otherBookingUserId.isEmpty()) {
                    for (String userId : otherBookingUserId) {
                        int willDeleteBookingId = applicantMap.get(userId);
                        for (int i = 0; i < newCanceledBooking.toArray().length; i++) {
                            if (newCanceledBooking.get(i).getBookingId() == willDeleteBookingId) {
                                allBookingId.remove(Integer.valueOf(willDeleteBookingId));
                                deleteBooking(willDeleteBookingId);
                            } else {
                                restCanceledBooking.add(canceledBooking.get(i));
                            }
                        }
                    }
                }

                List<Integer> finalBookingId = allBookingId;
                // 남은 cancel되었던 예약들 중 같은 시간대 예약자 존재 시 삭제
                if (restCanceledBooking.isEmpty()) {
                    restCanceledBooking = newCanceledBooking;
                }

                if (!allBookingId.isEmpty()) {
                    for (int bookingId : allBookingId) {
                        String startTime = null;
                        String endTime = null;
                        List<String> userIds = new ArrayList<>();

                        for (int i = 0; i < restCanceledBooking.toArray().length; i++) {
                            if (restCanceledBooking.get(i).getBookingId() == bookingId) {
                                userIds.add(restCanceledBooking.get(i).getUserId());
                                startTime = restCanceledBooking.get(i).getStartTime();
                                endTime = restCanceledBooking.get(i).getEndTime();
                            }
                        }

                        if (!userIds.isEmpty()) { // 같은 시간대 예약되었는지 확인 후 있을 경우 기존 취소되었던 예약 목록에서 삭제
                            String usersIdString = userService.changeString(null, userIds);
                            int existSameTimeBooking = bookingMapper.findCancelSameBooking(today, usersIdString, startTime, endTime);
                            if (existSameTimeBooking != 0) {
                                finalBookingId.remove(Integer.valueOf(bookingId));
                                deleteBooking(bookingId);
                            }
                        }
                    }

                    // 위의 조건에 모두 부합할 경우 취소되었던 예약 활성화
                    String selectForBookingsId = addBookingId(finalBookingId);

                    if (selectForBookingsId != null) {
                        int updateResultCount = bookingMapper.updateBookingMode(selectForBookingsId, null);
                    }

                }
            }
        }
        // 공식일정 취소
        return deleteBooking(booking.getBookingId());
    }


    // 공식일정으로 인한 예약 수정
    public int updateBooking(int roomId, String userId, String startTime, String endTime, boolean official) {
        Date today = time();
        List<Integer> sameBooking = bookingMapper.findSameBooking(today, roomId, startTime, endTime);

        if (!sameBooking.isEmpty()){
            String bookingsIdString = addBookingId(sameBooking);
            int updateResultCount = bookingMapper.updateBookingMode(bookingsIdString, "cancel");
        }
        int resultBookingId = insertBooking(roomId, startTime, endTime, official);
        return resultBookingId;
    }

}
