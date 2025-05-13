package com.longjunwang.finbotplus.mapper;

import com.longjunwang.finbotplus.entity.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecordMapper {
    int insertSelective(Record record);

    int updateSelective(Record record);

    int deleteByRecordNo(@Param("recordNo") String recordNo);

    Record selectByRecordNo(@Param("recordNo") String recordNo);

    List<Record> selectByCondition(Record record);

    List<Record> selectByRangeDate(@Param("startTime") String startTime, @Param("endTime") String endTime);
    List<Record> selectByRangeDateAndRemark(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("remark")String remark);
}
