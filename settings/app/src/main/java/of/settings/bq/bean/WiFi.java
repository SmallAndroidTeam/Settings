package of.settings.bq.bean;

public class WiFi {
    private String ssid;
    private int signal;  // Valid value is 0, 1, 2
    private int networkId;
    private String status;
    private String security;

    public WiFi() {
        this.ssid = null;
        this.signal = -1;
        this.networkId = -1;
        this.status = "未连接";
        this.security = null;
    }

    public WiFi(String ssid, int signal, int networkId, String status, String security) {
        this.ssid = ssid;
        this.signal = signal;
        this.networkId = networkId;
        this.status = status;
        this.security = security;
    }

    @Override
    public String toString() {
        return "WiFi{ " +
                "ssid='" + ssid + '\'' +
                ", signal='" + signal + '\'' +
                ", networkId='" + networkId + '\'' +
                ", status='" + status + '\'' +
                ", security='" + security + '\'' +
                " }";
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public int getNetworkId() {
        return networkId;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }
}
