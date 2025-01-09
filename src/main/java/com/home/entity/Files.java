package com.home.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Date;

/**
 * @Author: zhazhaming
 * @Date: 2024/10/03/22:57
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="Files对象", description="")
@TableName("files")
public class Files {

    @TableField("file_id")
    String file_id;

    @TableField("file_name")
    String file_name;

    @TableField("file_type")
    String file_type;

    @TableField("file_size")
    Long file_size;

    @TableField("upload_date")
    Date upload_date;

    @TableField("upload_user_id")
    Integer upload_user_id;

    @TableField("file_path")
    String file_path;

    @TableField("is_public")
    Integer is_public;
}
