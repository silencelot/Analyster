package com.elle.analyster.presentation.filter;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.*;

/**
 * CLASS 
 * ActionCheckListModel
 * @param <T> 
 */
public class ActionCheckListModel<T> implements ICheckListModel<T>{
    
    // attributes
    private final List<ListDataListener> listeners = Collections.synchronizedList( new ArrayList<ListDataListener>());
    private final DefaultCheckListModel<T> originalModel;
    private final CheckAll<T> actionCheckAll = new CheckAll<T>();
    private final List<CheckAll<T>> actionItems = Arrays.asList( actionCheckAll );
    private final Set<CheckAll<T>> checks = new HashSet<CheckAll<T>>();
    
    /**
     * CONSTRUCTOR
     * ActionCheckListModel
     * Looks like it just adds a listListener
     * The List of ListDataListeners does not make sense since this is only called
     * once and each time is a new instance
     * @param originalModel 
     */
    public ActionCheckListModel( final DefaultCheckListModel<T> originalModel ) {
        
        this.originalModel = originalModel;
        
        //react on original model changes
        this.originalModel.addListDataListener( new ListDataListener () {
            
            @Override
            public void intervalAdded(ListDataEvent e) {
                ListDataEvent event = toDecoratedEvent(e);
                for( ListDataListener l: listeners ) {
                    l.intervalAdded(event);
                }
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                ListDataEvent event = toDecoratedEvent(e);
                for( ListDataListener l: listeners ) {
                    l.intervalRemoved(event);
                }
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                ListDataEvent event = toDecoratedEvent(e);
                for( ListDataListener l: listeners ) {
                    l.contentsChanged(event);
                }
                if ( originalModel.getCheckedItems().size() < originalModel.getSize() ) {
                    checks.remove(actionCheckAll);
                }  else {
                    checks.add(actionCheckAll);
                }
                fireListDataChanged();
            }
        });
    }
    
    /**
     * getSize
     * @return 
     * 5 - occurances / 3 classes: action, checkall, checklist
     */
    @Override
    public int getSize() {
        return originalModel.getSize() + actionItems.size();
    }

    /**
     * getElementAt
     * @param index
     * @return 
     * 
     */
    @Override
    public Object getElementAt(int index) {
        if ( isDecoratedIndex(index)) {
            return actionItems.get(index);
        } else {
            return originalModel.getElementAt( toOriginalIndex(index));
        }
    }
    
    /**
     * toOriginalIndex
     * @param index
     * @return 
     *  3 occurances this class
     */
    private int toOriginalIndex( int index ) {
        return index - actionItems.size();
    }

    /**
     * toDecoratedIndex
     * @param index
     * @return 
     * 2 occurances this class
     */
    private int toDecoratedIndex( int index ) {
        return index + actionItems.size();
    }
    
    /**
     * isDecoratedIndex
     * @param index
     * @return 
     *  3 occurances this class
     */
    private boolean isDecoratedIndex( int index ) {
        int size = actionItems.size();
        return size > 0 && index >= 0 && index < size; 
    }
    
    /**
     * addListDataListener
     * @param l 
     * once this class
     */
    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    /**
     * removeListDataListener
     * @param l 
     * no occurances
     */
    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }
    
    /**
     * fireListDataChanged
     */
    private void fireListDataChanged() {
        ListDataEvent e = new ListDataEvent( this, 0, 0, getSize() );
        for( ListDataListener l: listeners ) {
            l.contentsChanged(e);
        }
    }

    /**
     * toDecoratedEvent
     * @param e
     * @return 
     */
    private ListDataEvent toDecoratedEvent( ListDataEvent e ) {
        return new ListDataEvent(
            e.getSource(), 
            e.getType(), 
            toDecoratedIndex(e.getIndex0()), 
            toDecoratedIndex(e.getIndex1()));
    }
    
    /**
     * isCheckedIndex
     * @param index
     * @return 
     */

    public boolean isCheckedIndex(int index) {
        if ( isDecoratedIndex(index)) {
            return checks.contains(actionItems.get(index));
        } else {
            return originalModel.isCheckedIndex( toOriginalIndex(index));
        }
        
    }

    /**
     * setCheckedIndex
     * @param index
     * @param value 
     */

    public void setCheckedIndex(int index, boolean value) {
        if ( isDecoratedIndex(index)) {
            
            // returns List<CheckAll<T>
            CheckAll<T> item = actionItems.get(index);
            item.check(originalModel, value);
            if ( value ) checks.add(item); else checks.remove(item);
            fireListDataChanged();
        } else {
             originalModel.setCheckedIndex( toOriginalIndex(index), value);
        }
    }

    /**
     * getCheckedItems
     * @return 
     */

    public Collection<T> getCheckedItems() {
        return originalModel.getCheckedItems();
    }

    /**
     * setCheckedItems
     * @param items 
     */

    public void setCheckedItems(Collection<T> items) {
        originalModel.setCheckedItems(items);
    }

    /**
     * filter
     * @param pattern
     * @param listFilter 
     */

    public void filter(String pattern, CheckListFilterType listFilter) {
        originalModel.filter(pattern, listFilter);
    }

}