/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tree;

/**
 * Class for tree structure. The methods are recursive for inserting
 * a child and performing DFS search. 
 * 
 *
 * @author Jakob
 */
public class Tree 
{
    private Node m_root;

    public Tree(int type)
    {
        this.m_root = null;
    }
    
    public void BuildTree(int key)
    {
        if(m_root != null)
            Insert(key, m_root);
        else
        {
            m_root = new Node();
            m_root.key = key;
            m_root.children = InsertChildren(key);
        }
    }
    
    private void Insert(int key, Node node)
    {
        for(int i = 0; i < node.children.length; i++)
        {
            if(node.children[i].children != null)
                Insert(key, node.children[i]);
            else
                node.children[i].children = InsertChildren(key);
        }
    }
       
    private Node[] InsertChildren(int key)
    {
        Node[] children;
        children = new Node[6];
        
        for(int i = 0; i < children.length; i++)
        {
            Node c = new Node();
            c.key = key;
            children[i] = c;
        }
            
        return children;
    }
    
//    public int DFS()
//    {
//        if(m_root != null)
//            DFS(m_root);
//        
//        return m_bestValue;
//    }
    
//    private void DFS(Node node)
//    {
//        if(node.children != null)
//        {
//            for(int i = 0; i < node.children.length; i++)
//            {
//                if(node.children[i].children != null)
//                    DFS(node.children[i]);
//                else
//                    m_bestValue = MinMaxSearch(node.children, m_bestValue);
//            }
//        }
//    }
    
//    private int MinMaxSearch(Node[] children, int minMaxValue)
//    {
//        int bestValue = minMaxValue;
//        
//        for(int i = 0; i < children.length; i++)
//        {
//            //Max value
//            if(children[i].key > bestValue && m_type == 1)
//                bestValue = children[i].key;
//            
//            //Min value
//            if((bestValue == 0 && m_type == 2) || (children[i].key < bestValue && m_type == 2))
//                bestValue = children[i].key;
//        } 
//        
//        return bestValue;
//    }
}
