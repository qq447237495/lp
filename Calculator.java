import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
 
public class Calculator {
 
    private BigDecimal preTotal; // 前面累计计算值
    private BigDecimal newNum; // 新计算值
    private List<BigDecimal> lastNumList = new ArrayList<>(); // 最近系列操作值
    private List<String> lastOptList = new ArrayList<>(); // 最近系列操作
    private List<BigDecimal> lastTotalList = new ArrayList<>(); // 最近系列总值
    private String curOperator; // 当前操作符
    private int lastOptIndex = -1; // undo/redo最近操作索引
    private int scale = 2; // 默认精度2位小数
    private int validIndexMax = -1; // undo/redo有效索引最大值
 
    public BigDecimal getPreTotal() {
        return preTotal;
    }
 
    public void setPreTotal(BigDecimal preTotal) {
        this.preTotal = preTotal;
    }
 
    public BigDecimal getNewNum() {
        return newNum;
    }
 
    public void setNewNum(BigDecimal newNum) {
        if(preTotal == null){ // 未计算过,累计总值为第一个输入值
            preTotal = newNum;
        }else{
            this.newNum = newNum;
        }
    }
 
    public List<BigDecimal> getLastNumList() {
        return lastNumList;
    }
 
    public void setLastNumList(List<BigDecimal> lastNumList) {
        this.lastNumList = lastNumList;
    }
 
    public List<String> getLastOptList() {
        return lastOptList;
    }
 
    public void setLastOptList(List<String> lastOptList) {
        this.lastOptList = lastOptList;
    }
 
    public String getCurOperator() {
        return curOperator;
    }
 
    public void setCurOperator(String curOperator) {
        this.curOperator = curOperator;
    }
 
    public int getScale() {
        return scale;
    }
 
    public void setScale(int scale) {
        this.scale = scale;
    }
 
    public List<BigDecimal> getLastTotalList() {
        return lastTotalList;
    }
 
    public void setLastTotalList(List<BigDecimal> lastTotalList) {
        this.lastTotalList = lastTotalList;
    }
 
    public int getLastOptIndex() {
        return lastOptIndex;
    }
 
    public void setLastOptIndex(int lastOptIndex) {
        this.lastOptIndex = lastOptIndex;
    }
 
    public int getValidIndexMax() {
        return validIndexMax;
    }
 
    public void setValidIndexMax(int validIndexMax) {
        this.validIndexMax = validIndexMax;
    }
 
    /**
     *  计算,相当于计算器的等于按钮
     */
    public void calc(){
        preTotal = preTotal == null ? BigDecimal.ZERO : preTotal;
        if(curOperator == null){
            System.out.println("请选择操作!");
        }
        if(newNum != null){ // 新输入值
            // 累加计算
            BigDecimal ret = calcTwoNum(preTotal, curOperator, newNum);
            if(this.lastOptIndex == -1){ // 未处于redo/undo中间过程
                lastTotalList.add(preTotal);
                lastNumList.add(newNum);
                lastOptList.add(curOperator);
            }else{ // 处于redo/undo中间过程,覆盖undo/redo操作记录,并记录有效索引最大值
                this.lastOptIndex++;
                this.validIndexMax = this.lastOptIndex;
                this.lastTotalList.set(this.lastOptIndex, ret);
                this.lastNumList.set(this.lastOptIndex-1, newNum);
                this.lastOptList.set(this.lastOptIndex-1, curOperator);
            }
            preTotal = ret;
            curOperator = null;
            newNum = null;
        }
    }
 
    /**
     * 回撤到上一步
     */
    public void undo(){
        if(preTotal != null && lastOptIndex == -1){ // 未进行undo/redo操作,存储最后计算结果
            lastTotalList.add(preTotal);
            curOperator = null;
            newNum = null;
        }
 
        if(lastTotalList.size() == 0){
            System.out.println("无操作!");
        }else if(lastTotalList.size() == 1){
            System.out.println("undo后值:0,"+"undo前值:"+preTotal);
            preTotal = BigDecimal.ZERO;
        } else {
            if(lastOptIndex == -1){
                lastOptIndex = lastOptList.size()-1;
            }else{
                if(lastOptIndex-1 < 0){
                    System.out.println("无法再undo!");
                    return;
                }
                lastOptIndex--;
            }
            cancelPreOperate(lastTotalList.get(lastOptIndex),lastOptList.get(lastOptIndex), lastNumList.get(lastOptIndex));
        }
    }
 
    /**
     *  根据回撤进行重做
     */
    public void redo(){
        try{
            if(lastOptIndex > -1){
                if(lastOptIndex + 1 == lastTotalList.size() || lastOptIndex+1 == this.validIndexMax+1){
                    System.out.println("无法再redo!");
                    return;
                }
                lastOptIndex++;
 
                redoOperate(lastTotalList.get(lastOptIndex),lastOptList.get(lastOptIndex-1), lastNumList.get(lastOptIndex-1));
            }
        }catch (Exception e){
            System.out.println("redo异常,lastOptIndex:"+lastOptIndex);
        }
    }
 
    private void redoOperate(BigDecimal redoTotal, String redoOpt, BigDecimal redoNum) {
        System.out.println("redo后值:"+redoTotal+",redo前值:"+preTotal+",redo的操作:"+redoOpt+",redo操作的值:"+redoNum);
        preTotal = redoTotal;
        curOperator = null;
        newNum = null;
    }
 
    private void cancelPreOperate(BigDecimal lastTotal, String lastOpt, BigDecimal lastNum) {
        System.out.println("undo后值:"+lastTotal+",undo前值:"+preTotal+",undo的操作:"+lastOpt+",undo操作的值:"+lastNum);
        preTotal = lastTotal;
        curOperator = null;
        newNum = null;
    }
 
    /**
     * 进行累计计算
     * @param preTotal 前面已累计值
     * @param curOperator 当前操作
     * @param newNum 新输入值
     * @return 计算结果
     */
    private BigDecimal calcTwoNum(BigDecimal preTotal, String curOperator, BigDecimal newNum) {
        BigDecimal ret = BigDecimal.ZERO;
        curOperator = curOperator == null ? "+" : curOperator;
        switch (curOperator){
            case "+":
                ret = preTotal.add(newNum);
                break;
            case "-":
                ret = preTotal.subtract(newNum).setScale(scale, RoundingMode.HALF_UP);
                break;
            case "*":
                ret = preTotal.multiply(newNum).setScale(scale, RoundingMode.HALF_UP);
                break;
            case "/":
                ret = preTotal.divide(newNum, RoundingMode.HALF_UP);
                break;
        }
        return ret;
    }
 
    /**
     * 显示操作结果
     */
    public String display(){
        StringBuilder sb = new StringBuilder();
        if(preTotal != null){
            sb.append(preTotal.setScale(scale, BigDecimal.ROUND_HALF_DOWN).toString());
        }
        if(curOperator != null){
            sb.append(curOperator);
        }
        if(newNum != null){
            sb.append(newNum);
        }
        System.out.println(sb.toString());
        return sb.toString();
    }
 
    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.setNewNum(new BigDecimal(3));
        calculator.setCurOperator("+");
        calculator.setNewNum(new BigDecimal(5));
        calculator.display();
        calculator.calc();
        calculator.display();
        calculator.setCurOperator("*");
        calculator.setNewNum(new BigDecimal(2));
        calculator.display();
        calculator.calc();
        calculator.display();
        calculator.undo();
        calculator.display();
 
        System.out.println("开始打断undo并附加额外计算:+2");
        calculator.setCurOperator("+");
        calculator.setNewNum(new BigDecimal(2));
        calculator.display();
        calculator.calc();
        calculator.display();
        System.out.println("打断计算结束,重新进行undo/redo操作!");
 
        calculator.undo();
        calculator.display();
        calculator.undo();
        calculator.display();
        calculator.redo();
        calculator.display();
        calculator.redo();
        calculator.display();
        calculator.redo();
        calculator.display();
        calculator.redo();
        calculator.display();
    }
 
}
