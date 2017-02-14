/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HashTable;

/**
 *
 * @author ostlinja
 */
public interface HashComparator<K> {
    public int hashIndex(K k);
    public int sHash(K k);
    public boolean keyEqual(K k1, K k2);
}
