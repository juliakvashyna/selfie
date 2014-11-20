package com.bigdropinc.selfieking.model.selfie;

import java.util.Arrays;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@DatabaseTable(tableName = "selfie")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelfieImage implements Parcelable {

    @DatabaseField(id = true, generatedId = false)
    private int id;
    @DatabaseField
    private String image;
    @DatabaseField
    private String token;
    @DatabaseField
    private String date;
    @DatabaseField
    private String description;
    @DatabaseField
    private int like;
    @DatabaseField
    private int comment;
    @DatabaseField
    private boolean liked;
    private byte[] bytesImage;

    public SelfieImage() {
    }

    public SelfieImage(int id) {
        super();
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLikes() {
        return like;
    }

    public void setLikes(int likes) {
        this.like = likes;
    }

    public void setComments(int comments) {
        this.comment = comments;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getBytesImage() {
        return bytesImage;
    }

    public void setBytesImage(byte[] bytesImage) {
        this.bytesImage = bytesImage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String url) {
        this.image = url;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "SelfieImage [id=" + id + ", image=" + image + ", token=" + token + " ]";
    }

    // public void convertBitmapToByte() {
    // ByteArrayOutputStream stream = new ByteArrayOutputStream();
    // bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    // bytesImage = stream.toByteArray();
    //
    // }
    //
    // public void convertByteToBitmap() {
    //
    // byte[] decodedByte = Base64.decode(bytesImage, 0);
    // bitmap = BitmapFactory.decodeByteArray(decodedByte, 0,
    // decodedByte.length);
    //
    // }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(image);
        dest.writeString(token);
        dest.writeString(description);
        dest.writeInt(like);
        dest.writeInt(comment);
        dest.writeByte((byte) (liked ? 1 : 0));
        dest.writeString(date);
        // dest.writeByteArray(bytesImage);
    }

    public static final Parcelable.Creator<SelfieImage> CREATOR = new Parcelable.Creator<SelfieImage>() {
        public SelfieImage createFromParcel(Parcel in) {
            return new SelfieImage(in);
        }

        public SelfieImage[] newArray(int size) {
            return new SelfieImage[size];
        }
    };

    private SelfieImage(Parcel in) {
        id = in.readInt();
        image = in.readString();
        token = in.readString();
        description = in.readString();
        like = in.readInt();
        comment = in.readInt();
        liked = in.readByte() != 0;
        date = in.readString();
        // in.readByteArray(bytesImage);
        // convertByteToBitmap();

    }

}
