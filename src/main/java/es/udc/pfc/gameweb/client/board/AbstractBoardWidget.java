/*
 * Copyright 2011 José Martínez
 * 
 * This file is part of GameWeb.
 *
 * GameWeb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * GameWeb is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GameWeb.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.udc.pfc.gameweb.client.board;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

import es.udc.pfc.gamelib.board.Board;
import es.udc.pfc.gamelib.board.Piece;
import es.udc.pfc.gamelib.board.Position;

public abstract class AbstractBoardWidget<P extends Piece> extends Composite implements BoardWidget<P>, ClickHandler {

	private static final int CELL_WIDTH = 100;
	private static final int CELL_HEIGHT = 100;
	
	//private final LayoutPanel panel;
	@Nullable private final Canvas canvas;
	@Nullable private final Context2d context;
	private final Map<Position, String> highlighted;

	@Nullable private Board<P> board;

	protected AbstractBoardWidget() {
		highlighted = Maps.newHashMap();
		
		canvas = Canvas.createIfSupported();
		if (canvas == null) {
			throw new RuntimeException("Canvas not supported");
		}
		canvas.addClickHandler(this);
		context = canvas.getContext2d();
		initWidget(canvas);
		//panel = new LayoutPanel();
		//panel.add(canvas);
		//initWidget(panel);
		
		/*Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				AbstractBoardWidget.this.onResize();
			}
		});*/
	}
	
	abstract protected ImageElement getPieceImage(P piece);
	
	/*@Override
	protected final void onLoad() {
		super.onLoad();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				onResize();
			}
		});
	}*/
	
	/*
	@Override
	public final void onResize() {
		if (board == null)
			return;

		final int panelWidth = panel.getOffsetWidth();
		final int panelHeight = panel.getOffsetHeight();

		final double targetAspectRatio = (double) (board.getNumberOfColumns() * CELL_WIDTH) / (double) (board.getNumberOfRows() * CELL_HEIGHT);
		final double aspectRatio = (double) panelWidth / (double) panelHeight;
		if (targetAspectRatio > aspectRatio) {
			final int widgetHeight = (int) (panelWidth / targetAspectRatio);
			final int widgetTop = (panelHeight - widgetHeight) / 2;
			
			panel.setWidgetLeftRight(canvas, 0, Unit.PX, 0, Unit.PX);
			panel.setWidgetTopHeight(canvas, widgetTop, Unit.PX, widgetHeight, Unit.PX);
			canvas.setPixelSize(panelWidth, widgetHeight);
		} else {
			final int widgetWidth = (int) (panelHeight * targetAspectRatio);
			final int widgetLeft = (panelWidth - widgetWidth) / 2;
			
			panel.setWidgetTopBottom(canvas, 0, Unit.PX, 0, Unit.PX);
			panel.setWidgetLeftWidth(canvas, widgetLeft, Unit.PX, widgetWidth, Unit.PX);
			canvas.setPixelSize(widgetWidth, panelHeight);
		}
		
		drawBoard();
	}
	*/
	
	/*@Override
	public final void onResize() {
		if (board == null)
			return;
		
		final double targetAspectRatio = (double) (board.getNumberOfColumns() * CELL_WIDTH) / (double) (board.getNumberOfRows() * CELL_HEIGHT);
		canvas.setPixelSize(620, (int) (620 / targetAspectRatio));
		
		drawBoard();
	}*/

	@Override
	public final void setBoard(final Board<P> board) {
		this.board = checkNotNull(board);
		
		canvas.setCoordinateSpaceWidth(board.getNumberOfColumns() * CELL_WIDTH);
		canvas.setCoordinateSpaceHeight(board.getNumberOfRows() * CELL_HEIGHT);
		
		canvas.setPixelSize(620, 620 * board.getNumberOfRows() / board.getNumberOfColumns());
		
		//onResize();
	}

	private final void fillSquare(final int col, final int row, final String style) {
		context.setFillStyle(style);
		context.fillRect((col - 1) * CELL_WIDTH, (board.getNumberOfRows() - row) * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
	}

	@Override
	public final void drawBoard() {
		if (board == null)
			return;
		
		context.clearRect(0, 0, board.getNumberOfColumns() * CELL_WIDTH, board.getNumberOfRows() * CELL_HEIGHT);

		for (int col = 1; col <= board.getNumberOfColumns(); col++) {
			for (int row = 1; row <= board.getNumberOfRows(); row++) {
				fillSquare(col, row, (col + row) % 2 == 0 ? "#ffce9e" : "#d18b47");
			}
		}

		for (final Entry<Position, String> hl : highlighted.entrySet()) {
			fillSquare(hl.getKey().getColumn(), hl.getKey().getRow(), hl.getValue());
		}

		for (final P piece : board.getAllPieces()) {
			final ImageElement ie = getPieceImage(piece);

			final int col = board.getPositionFor(piece).getColumn();
			final int row = board.getPositionFor(piece).getRow();

			context.drawImage(ie, (col - 1) * CELL_WIDTH, (board.getNumberOfRows() - row) * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
		}
	}

	@Override
	public final void onClick(final ClickEvent event) {
		if (board == null)
			return;
		
		final int cellw = canvas.getOffsetWidth() / board.getNumberOfColumns();
		final int cellh = canvas.getOffsetHeight() / board.getNumberOfRows();
		
		final Position clicked = new Position(1 + event.getX() / cellw, board.getNumberOfRows() - event.getY() / cellh);

		if (board.isValidPosition(clicked)) {
			fireEvent(new PositionClickedEvent(clicked));
		}
	}
	
	@Override
	public final void clearHighlights() {
		highlighted.clear();
		drawBoard();
	}
	
	@Override
	public final void highlightPosition(final Position position, final String format) {
		if (board == null)
			return;
		
		if (board.isValidPosition(position)) {
			highlighted.put(position, format);
		}
		
		drawBoard();
	}

	@Override
	public final HandlerRegistration addPositionClickedHandler(final PositionClickedEvent.Handler handler) {
		return addHandler(handler, PositionClickedEvent.TYPE);
	}
	
}
