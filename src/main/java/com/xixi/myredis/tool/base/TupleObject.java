package com.xixi.myredis.tool.base;

/**
 * 带泛型的Tuple，用于增强型redis zset中，返回数据库的数据
 */
public class TupleObject<T> {



    T member;

    Long score;

    public TupleObject(T member, Long score){
        this.member = member;
        this.score = score;
    }

    public T getMember() {
        return member;
    }

    public void setMember(T member) {
        this.member = member;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }


}
