package com.ablanco.pokemonstats;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

final class PokemonInfos implements Serializable {

    private static final int MIN_NOTATION = 100;
    private static final int MAX_NOTATION = 720;

    private String name;
    private String type;
    private int height;
    private int baseXP;
    private int weight;
    private int HP;
    private int defence;
    private int speDefence;
    private int attack;
    private int speAttack;
    private int speed;


    public PokemonInfos(String name, String type, int height, int baseXP, int weight, int HP, int defence, int speDefence, int attack, int speAttack, int speed) {
        this.name = name;
        this.speed = speed;
        this.type = type;
        this.height = height;
        this.baseXP = baseXP;
        this.weight = weight;
        this.HP = HP;
        this.defence = defence;
        this.speDefence = speDefence;
        this.attack = attack;
        this.speAttack = speAttack;
    }

    protected PokemonInfos(Parcel in) {
        name = in.readString();
        type = in.readString();
        height = in.readInt();
        baseXP = in.readInt();
        weight = in.readInt();
        HP = in.readInt();
        defence = in.readInt();
        speDefence = in.readInt();
        attack = in.readInt();
        speAttack = in.readInt();
        speed = in.readInt();
    }

    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public String getType() {
        return type;
    }

    public int getHeight() {
        return height;
    }

    public int getBaseXP() {
        return baseXP;
    }

    public int getWeight() {
        return weight;
    }

    public int getHP() {
        return HP;
    }

    public int getDefence() {
        return defence;
    }

    public int getSpeDefence() {
        return speDefence;
    }

    public int getAttack() {
        return attack;
    }

    public int getSpeAttack() {
        return speAttack;
    }

    public float getRating(){
        int sum = attack + defence + HP + speed + speAttack + speDefence;
        return (float)(sum - MIN_NOTATION) / (float)(MAX_NOTATION-MIN_NOTATION);
    }

}
