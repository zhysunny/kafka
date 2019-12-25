#!/usr/bin/env bash

# Author      : 章云
# Date        : 2019/9/1 12:37
# Description : 针对key=value配置文件的操作

Usage()
{
    echo "Usage: "
    echo "sh properties_config_tools.sh grepCount <fileName> <propertyName>"
    echo "sh properties_config_tools.sh contains <fileName> <propertyName>"
    echo "sh properties_config_tools.sh get <fileName> <propertyName>"
    echo "sh properties_config_tools.sh put <fileName> <propertyName> <propertyValue> <is export:0 no|1 yes>"
    exit 1
}

function grepCount(){
    if [[ $# -ne 2 ]]
    then
        Usage
        exit 1
    fi
    FILE_NAME=$1
    # 这里的PROPERTY_NAME是一行中的关键词，不一定是key值，必须是文件中唯一，否则会出现错误
    PROPERTY_NAME=$2
    declare -a result
    result=(`cat ${FILE_NAME} | grep ${PROPERTY_NAME} | grep -v "^#"`)
    echo ${#result[*]}
}

function contains(){
    count=`grepCount $*`
    if [[ ${count} -eq 0 ]]
    then
        echo "false"
    else
        echo "true"
    fi
}

function get(){
    if [[ $# -ne 2 ]]
    then
        Usage
        exit 1
    fi
    FILE_NAME=$1
    # 这里的PROPERTY_NAME是一行中的关键词，不一定是key值，必须是文件中唯一，否则会出现错误
    PROPERTY_NAME=$2
    PROPERTY_VALUE=`awk -F= "{if(\\$1~/^${PROPERTY_NAME}$/) print \\$2}" ${FILE_NAME}`
    if [[ -z ${PROPERTY_VALUE} ]]
    then
        PROPERTY_VALUE=`awk -F= "{if(\\$1~/^export ${PROPERTY_NAME}$/) print \\$2}" ${FILE_NAME}`
    fi
    if [[ -z ${PROPERTY_VALUE} ]]
    then
        PROPERTY_VALUE=`grep "^${PROPERTY_NAME}" ${FILE_NAME} | awk '{print $2}'`
    fi
    echo ${PROPERTY_VALUE}
}

function put(){
    # 注意：对于环境变量PATH=${PATH}中${PATH}会被解析，使用时一定要给$加转义符号
    if [[ $# != 3 && $# != 4 ]]
    then
        Usage
        exit 1
    fi
    FILE_NAME=$1
    # 这里的PROPERTY_NAME是一行中的关键词，不一定是key值，必须是文件中唯一，否则会出现错误
    PROPERTY_NAME=$2
    PROPERTY_VALUE=$3
    count=`grepCount ${FILE_NAME} ${PROPERTY_NAME}`
    # $4 = 1 表示设置环境变量，需要加export
    # $4 = 2 表示设置变量为key value，中间是空格不是等于号
    # 其他情况下默认就是key=value的形式
    if [[ ${count} -eq 0 ]]
    then
        # 没找到则添加
        if [[ $4 -eq 1 ]]
        then
            # 环境变量HOME配置在PATH前面
            PATH_LINE=`grep -n "^export PATH=" ${FILE_NAME} | head -1 | cut -d ":" -f 1`
            if [[ -z "${PATH_LINE}" ]]
            then
                echo "export ${PROPERTY_NAME}=${PROPERTY_VALUE}" >> ${FILE_NAME}
            else
                sed -i "${PATH_LINE}i\export ${PROPERTY_NAME}=${PROPERTY_VALUE}" ${FILE_NAME}
            fi
        elif [[ $4 -eq 2 ]]
        then
            echo "${PROPERTY_NAME} ${PROPERTY_VALUE}" >> ${FILE_NAME}
        else
            echo "${PROPERTY_NAME}=${PROPERTY_VALUE}" >> ${FILE_NAME}
        fi
        echo "add $FILE_NAME $PROPERTY_NAME=$PROPERTY_VALUE successful !!"
    else
        PROPERTY_VALUE=`echo ${PROPERTY_VALUE} | sed 's#\/#\\\/#g'`
        if [[ $4 -eq 1 ]]
        then
            sed -i "s/^export ${PROPERTY_NAME}=.*/export ${PROPERTY_NAME}=${PROPERTY_VALUE}/g" ${FILE_NAME}
        elif [[ $4 -eq 2 ]]
        then
            sed -i "s/^${PROPERTY_NAME} .*/${PROPERTY_NAME} ${PROPERTY_VALUE}/g" ${FILE_NAME}
        else
            sed -i "s/^${PROPERTY_NAME}=.*/${PROPERTY_NAME}=${PROPERTY_VALUE}/g" ${FILE_NAME}
        fi
        echo "modify $FILE_NAME $PROPERTY_NAME=$PROPERTY_VALUE successful !!"
    fi
}

COMMAND=$1
#参数位置左移1
shift

case ${COMMAND} in
    grepCount )
        grepCount $*
        ;;
    contains )
        contains $*
        ;;
    get )
        get $*
        ;;
    put )
        put $*
        ;;
    * )
		Usage
        exit 1
        ;;
esac
